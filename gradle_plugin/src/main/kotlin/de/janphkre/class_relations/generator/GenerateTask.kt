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
    private lateinit var destinationPathFromSource: String
    private lateinit var sourceDirectoryFile: File
    private lateinit var generatedFileName: String
    private lateinit var packagePrefix: List<String>

    private fun File.toRelativeForwardSlashString(base: File): String {
        val relative = base.toPath().relativize(this.toPath())
        val result = relative.joinToString("/")
        println("RelPath from ${base.absolutePath} to ${this.absolutePath} is $result")
        return result
    }

    @TaskAction
    fun action() {
        initializeFields()
        registerKlassFilters()

        val parser = KotlinParser.getInstance()
        val sourceDir = source.get()
        Sequence { SortedFileTreeWalker(sourceDir, onLeave = { dir ->
            val childPackages = dir.subDirectories.map { it.name }
            //TODO FILTER OUT empty path?
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
        sourceDirectoryFile = source.get()
        destinationPathFromSource = sourceDirectoryFile.toRelativeForwardSlashString(destination.get())
        generator = ClassRelationsPumlGenerator.getInstance(
            settings = settings
        )
        itemFactory = KlassItemFactory.getInstance()
        generatedFileName = settings.generatedFileName
        packagePrefix = settings.projectPackagePrefix.split('.')
    }

    private fun registerKlassFilters() {
        val filterFactory = KlassFilterFactory.getInstance()
        val klassFilters = filterFactory.createFilters(filters.get())
        itemFactory.applyFilters(klassFilters)
    }

    private fun KotlinParser.readDefinition(file: File) {
        val definition = parse(file.readText(), file.nameWithoutExtension, filePath = file.toRelativeForwardSlashString(sourceDirectoryFile))
        definitions.add(definition ?: return)
    }

    private fun generateDiagram(childPackages: List<String>, destinationDiagramPath: String) {
        val pumlDiagram = if (definitions.isEmpty()) {
            val diagramPackage = destinationDiagramPath.split('/')
            if (packagePrefix.startsWithButNotEqual(diagramPackage)) {
                return
            }
            generator.generateEmpty(diagramPackage, childPackages, destinationPathFromSource)
        } else {
            generator.generate(definitions, childPackages, destinationPathFromSource)
        }

        val destinationFile = File(destination.get(), "${destinationDiagramPath}/${generatedFileName}")
        destinationFile.parentFile.mkdirs()
        destinationFile.createNewFile()
        destinationFile.writeText(pumlDiagram)
        definitions.clear()
        itemFactory.clear()
    }

    private fun List<String>.startsWithButNotEqual(other: List<String>): Boolean {
        if (other.size >= this.size) {
            return false
        }
        val otherIter = other.iterator()
        val thisIter = this.iterator()
        while (otherIter.hasNext()) {
            if (otherIter.next() != thisIter.next()) {
                return false
            }
        }
        return true
    }
}