package de.janphkre.class_relations.generator

import de.janphkre.class_relations.generator.filetree.SortedFileTreeWalker
import de.janphkre.class_relations.library.data.filter.KlassFilterFactory
import de.janphkre.class_relations.library.data.item.KlassItemFactory
import de.janphkre.class_relations.library.domain.ClassRelationsPumlGenerator
import de.janphkre.class_relations.library.domain.KotlinParser
import de.janphkre.class_relations.library.model.KlassWithRelations
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
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

    @get:Input
    abstract val filters: ListProperty<String>

    private val definitions = ArrayList<KlassWithRelations>()
    private lateinit var generator: ClassRelationsPumlGenerator
    private lateinit var itemFactory: KlassItemFactory
    private lateinit var destinationPathFromModule: String
    private lateinit var moduleDirectoryFile: File
    private lateinit var generatedFileName: String

    private fun File.toRelativeForwardSlashString(base: File): String {
        val relative = base.toPath().relativize(this.toPath())
        return relative.joinToString("/")
    }

    @TaskAction
    fun action() {
        initializeFields()
        registerKlassFilters()

        val parser = KotlinParser.getInstance()
        val sourceDir = source.get()
        Sequence { SortedFileTreeWalker(sourceDir, onLeave = { dir ->
            val childPackages = dir.subDirectories.map { it.name }
            generateDiagram(
                childPackages,
                dir.directory.toRelativeForwardSlashString(sourceDir)
            )
        }) }
            .filter { it.extension == "kt" }
            .forEach { path ->
                parser.readDefinition(path)
            }
    }

    private fun initializeFields() {
        val settings = generatorSettings.get()
        moduleDirectoryFile = moduleDirectory.get()
        destinationPathFromModule = moduleDirectoryFile.toRelativeForwardSlashString(destination.get())
        generator = ClassRelationsPumlGenerator.getInstance(
            settings = settings
        )
        itemFactory = KlassItemFactory.getInstance()
        generatedFileName = settings.generatedFileName
    }

    private fun registerKlassFilters() {
        val filterFactory = KlassFilterFactory.getInstance()
        val klassFilters = filterFactory.createFilters(filters.get())
        itemFactory.applyFilters(klassFilters)
    }

    private fun KotlinParser.readDefinition(file: File) {
        val definition = parse(file.readText(), file.nameWithoutExtension, filePath = file.toRelativeForwardSlashString(moduleDirectoryFile))
        definitions.add(definition ?: return)
    }

    private fun generateDiagram(childPackages: List<String>, destinationDiagramPath: String) {
        val pumlDiagram = if (definitions.isEmpty()) {
            generator.generateEmpty(destinationDiagramPath.split('/'), childPackages, destinationPathFromModule)
        } else {
            generator.generate(definitions, childPackages, destinationPathFromModule)
        }

        val destinationFile = File(destination.get(), "${destinationDiagramPath}/${generatedFileName}")
        destinationFile.parentFile.mkdirs()
        destinationFile.createNewFile()
        destinationFile.writeText(pumlDiagram)
        definitions.clear()
        itemFactory.clear()
    }
}