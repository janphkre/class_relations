/**
 *    Copyright 2025 Jan Phillip Kretzschmar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.janphkre.class_relations.generator

import de.janphkre.class_relations.generator.filetree.FileInRoot
import de.janphkre.class_relations.generator.filetree.MultiRootFileTreeWalker
import de.janphkre.class_relations.library.data.filter.KlassDisabledFiltering
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
import java.util.Locale

abstract class GenerateTask: DefaultTask() {

    @get:OutputDirectory
    abstract val destination: Property<File>

    @get:Input
    abstract val sources: ListProperty<File>

    @get:Input
    abstract val generatorSettings: Property<ClassRelationsPumlGenerator.Settings>

    @get:Input
    abstract val filters: ListProperty<String>

    private val definitions = ArrayList<KlassWithRelations>()
    private lateinit var generator: ClassRelationsPumlGenerator
    private lateinit var itemFactory: KlassItemFactory
    private lateinit var disabledFiltering: KlassDisabledFiltering
    private lateinit var generatedFileName: String
    private lateinit var packagePrefix: List<String>

    private fun File.toRelativeForwardSlashString(base: File): String {
        val relative = base.toPath().relativize(this.toPath())
        val result = relative.joinToString(PATH_SEPARATOR)
        return result
    }

    @TaskAction
    fun action() {
        initializeFields()
        registerKlassFilters()

        val parser = KotlinParser.getInstance()
        val sourceDirs = sources.get()
        val destinationDir = destination.get()
        val sourceAssociations = sourceDirs.associateWith { dir ->
            val sourcePath = dir.toRelativeForwardSlashString(destinationDir)
            val name = sourcePath
                .split(PATH_DELIMITER)
                .filterNot { it == ".." }
                .joinToString("") { it.capitalized() }
            (name to sourcePath)
        }
        println("Associations:\n${sourceAssociations.entries.joinToString("\n"){"\"${it.key}\"=\"${it.value.first}\";\"${it.value.second}\""}}")
        val sourceNames = sourceAssociations.mapValues { it.value.first }
        val sourcePathsFromDestination = sourceAssociations.values.associate { it.first to it.second }
        Sequence { MultiRootFileTreeWalker(sourceDirs, onLeave = { directory ->
            val childPackages = directory.subDirectories.keys
            val aDirectory = directory.directories.first()
            generateDiagram(
                childPackages,
                destinationDiagramFilePath = aDirectory.file
                    .toRelativeForwardSlashString(aDirectory.root),
                sourcePathsFromDestination
            )
        }) }
            .filter { it.file.extension == KOTLIN_FILE_TYPE }
            .forEach { file ->
                parser.readDefinition(file, sourceNames)
            }
    }

    private fun String.capitalized(): String {
        return replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

    private fun initializeFields() {
        val settings = generatorSettings.get()
        generator = ClassRelationsPumlGenerator.getInstance(
            settings = settings
        )
        disabledFiltering = KlassDisabledFiltering.getInstance()
        itemFactory = KlassItemFactory.getInstance()
        generatedFileName = settings.generatedFileName
        packagePrefix = settings.projectPackagePrefix.split(PACKAGE_DELIMITER)
    }

    private fun registerKlassFilters() {
        val filterFactory = KlassFilterFactory.getInstance()
        val klassFilters = filterFactory.createFilters(filters.get())
        itemFactory.applyFilters(klassFilters)
    }

    private fun KotlinParser.readDefinition(file: FileInRoot, sourceNames: Map<File, String>) {
        val definition = parse(
            fileContent = file.file.readText(),
            presentableName = file.file.nameWithoutExtension,
            filePathInRoot = file.file.toRelativeForwardSlashString(file.root),
            rootName = sourceNames[file.root]!!
        ) ?: return
        definitions.add(definition)
    }

    private fun generateDiagram(
        childPackages: Collection<String>,
        destinationDiagramFilePath: String,
        sourcePathsFromDestination: Map<String, String>
    ) {
        val filteredKlassItems = disabledFiltering.filter(definitions)
        val pumlDiagram = if (filteredKlassItems.isEmpty()) {
            val diagramPackage = destinationDiagramFilePath.split(PATH_DELIMITER)
            if (packagePrefix.startsWithButNotEqual(diagramPackage) || destinationDiagramFilePath.isBlank()) {
                return
            }
            generator.generateEmpty(diagramPackage, childPackages)
        } else {
            generator.generate(filteredKlassItems, childPackages, sourcePathsFromDestination)
        }
        val destinationFile = File(destination.get(), "${destinationDiagramFilePath}/${generatedFileName}")
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

    private companion object {
        private const val PACKAGE_DELIMITER = '.'
        private const val PATH_DELIMITER = '/'
        private const val KOTLIN_FILE_TYPE = "kt"
        private const val PATH_SEPARATOR = "/"
    }
}