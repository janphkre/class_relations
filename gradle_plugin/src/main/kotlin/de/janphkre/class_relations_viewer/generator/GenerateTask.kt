package de.janphkre.class_relations_viewer.generator

import de.janphkre.class_relations_viewer.library.domain.ClassRelationsPumlGenerator
import de.janphkre.class_relations_viewer.library.domain.KotlinHeaderParser
import de.janphkre.class_relations_viewer.library.model.KlassWithRelations
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class GenerateTask: DefaultTask() {

    @get:OutputDirectory
    abstract val destination: Property<File>

    @get:Internal
    abstract val moduleDirectory: Property<File>

    @get:InputDirectory
    abstract val source: Property<File>

    @get:Input
    abstract val generatorSettings: Property<ClassRelationsPumlGenerator.Settings>

    private val definitions = ArrayList<KlassWithRelations>()
    private lateinit var generator: ClassRelationsPumlGenerator
    private lateinit var destinationPathFromModule: String

    @TaskAction
    fun action() {
        destinationPathFromModule = moduleDirectory.get().toRelativeString(destination.get())
        generator = ClassRelationsPumlGenerator(
            generatorSettings = generatorSettings.get()
        )
        val parser = KotlinHeaderParser()
        val sourceDir = source.get()
        sourceDir.walkBottomUp()
            .onEnter {
                println("Entering $it")
                if (definitions.isNotEmpty()) {
                    println("Entered directory $it before all definitions of parent(?) were generated!")
                }
                generateDiagram(it.toRelativeString(sourceDir))
                true
            }
            .onLeave {
                println("Leaving $it")
                generateDiagram(it.toRelativeString(sourceDir))
            }
            .forEach { file ->
                val definition = parser.parse(file.readText(), file.nameWithoutExtension)
                definitions.add(definition?.toKlassWithRelations() ?: return@forEach)
            }
    }

    private fun generateDiagram(destinationDiagramPath: String) {
        if (definitions.isEmpty()) {
            //TODO: GENERATE EMPTY DIAGRAM FOR DIRECTORIES!
            return
        }
        val pumlDiagram = generator.generate(definitions, destinationPathFromModule)
        val destinationFile = File(destination.get(), destinationDiagramPath))
        destinationFile.parentFile.mkdirs()
        destinationFile.writeText(pumlDiagram)
        definitions.clear()
    }
}