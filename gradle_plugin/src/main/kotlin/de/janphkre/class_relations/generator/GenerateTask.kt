package de.janphkre.class_relations.generator

import de.janphkre.class_relations.generator.filetree.SortedFileTreeWalker
import de.janphkre.class_relations.library.domain.ClassRelationsPumlGenerator
import de.janphkre.class_relations.library.domain.KotlinParser
import de.janphkre.class_relations.library.model.KlassWithRelations
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
    private lateinit var moduleDirectoryFile: File

    @TaskAction
    fun action() {
        val settings = generatorSettings.get()
        moduleDirectoryFile = moduleDirectory.get()
        destinationPathFromModule = moduleDirectoryFile.toRelativeString(destination.get())
        generator = ClassRelationsPumlGenerator.getInstance(
            settings = settings
        )
        val parser = KotlinParser.getInstance()
        val sourceDir = source.get()
        Sequence { SortedFileTreeWalker(sourceDir, onLeave = {
            generateDiagram("${it.toRelativeString(sourceDir)}/${settings.generatedFileName}")
        }) }
            .filter { it.extension == "kt" }
            .forEach { path ->
                parser.readDefinition(path)
            }
    }

    private fun KotlinParser.readDefinition(file: File) {
        val definition = parse(file.readText(), file.nameWithoutExtension, filePath = file.toRelativeString(moduleDirectoryFile))
        definitions.add(definition ?: return)
    }

    private fun generateDiagram(destinationDiagramPath: String) {
        if (definitions.isEmpty()) {
            //TODO: GENERATE EMPTY DIAGRAM FOR EMPTY DIRECTORIES? (especially root directory?) -> would need to change file tree walker to bottom up instead of top down
            return
        }
        val pumlDiagram = generator.generate(definitions, destinationPathFromModule)
        val destinationFile = File(destination.get(), destinationDiagramPath)
        destinationFile.parentFile.mkdirs()
        destinationFile.writeText(pumlDiagram)
        definitions.clear()
    }
}