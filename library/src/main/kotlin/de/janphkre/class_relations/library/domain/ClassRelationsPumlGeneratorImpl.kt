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
package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.*
import java.util.*

internal class ClassRelationsPumlGeneratorImpl(
    private val generatorSettings: ClassRelationsPumlGenerator.Settings,
    private val sourcesLinks: Map<String, String>,
    private val externalLinks: Map<String, String>,
) : ClassRelationsPumlGenerator {

    private val projectBasePrefix = generatorSettings.projectBasePrefix.split('.')
    private val projectPackagePrefix = generatorSettings.projectPackagePrefix.split('.')
    private val projectRemainderPrefix = projectPackagePrefix.drop(projectBasePrefix.size)
    private val openPackages = Stack<String>()
    private var packageIndex = 0

    override fun generate(
        klasses: List<KlassWithRelations>,
        childPackages: Collection<String>,
    ): String {
        packageIndex = 0
        return StringBuilder(generatorSettings.initialCapacitySize * klasses.size).apply {
            appendContent("@startuml")
            //STRUCTURE
            val anyKlassPackage = klasses.first().item.filePackage
            createReferences(anyKlassPackage, sourcesLinks)
            createSelfPackageStructure(anyKlassPackage)
            createClasses(klasses)
            createImports(klasses, childPackages)
            appendLine()
            if (openPackages.isNotEmpty()) {
                println(this.toString())
                throw IllegalStateException("Should have closed all packages, BUG in PUML Generator?!")
            }
            //CONNECTIONS
            createConnections(klasses)

            appendContent("@enduml")
        }.toString()
    }

    override fun generateEmpty(
        filePackage: List<String>,
        childPackages: Collection<String>
    ): String {
        packageIndex = 0
        return StringBuilder(generatorSettings.initialCapacitySize).apply {
            appendContent("@startuml")
            createReferences(filePackage, emptyMap())
            createSelfPackageStructure(filePackage)
            createImports(emptyList(), childPackages)
            for( i in 0 until openPackages.size) {
                closePackage()
            }
            appendContent("@enduml")
        }.toString()
    }

    private fun StringBuilder.createReferences(filePackage: List<String>, sourcesLinks: Map<String, String>) {
        val pathDepth = filePackage.size
        val filePathReverse = "..".repeat(pathDepth, PATH_DELIMITER)
        appendContent("!\$pathToRoot = \"${filePathReverse}\"")
        for ((codeBaseName, link) in sourcesLinks) {
            appendContent("!\$pathToSource${codeBaseName} = \"${filePathReverse}${PATH_DELIMITER}${link}\"")
        }
        appendContent("!\$pathToDocsBase = \"${filePathReverse}${PATH_DELIMITER}${generatorSettings.projectPackagePrefix.replace(PACKAGE_DELIMITER, PATH_DELIMITER)}\"")
    }

    private fun StringBuilder.createSelfPackageStructure(filePackage: List<String>) {
        if (!filePackage.hasProjectPrefix()) {
            beginSelfPackage(filePackage.joinToString(PACKAGE_SEPARATOR))
            return
        }
        if (projectBasePrefix.size == filePackage.size) {
            beginSelfPackage(filePackage.joinToString(PACKAGE_SEPARATOR))
            return
        }
        var qualifiedName = projectBasePrefix
        beginPackage(generatorSettings.projectBasePrefix, qualifiedName)
        val packages = filePackage.drop(projectBasePrefix.size)
        var packageLink = ""
        for (i in 0 until packages.size - 1) {
            packageLink += "/${packages[i]}"
            qualifiedName = qualifiedName + packages[i]
            beginPackage(packages[i], qualifiedName)
        }
        beginSelfPackage(packages.last())
    }

    private fun String.repeat(count: Int, separator: Char): CharSequence {
        return when (count) {
            0 -> ""
            1 -> this
            else -> {
                val sb = StringBuilder(count * (length + 1) - 1)
                for (i in 2..count) {
                    sb.append(this)
                    sb.append(separator)
                }
                sb.append(this)
                sb.toString()
            }
        }
    }

    private fun List<String>.hasProjectPrefix(): Boolean {
        if (size < projectBasePrefix.size) {
            return false
        }
        return projectBasePrefix.withIndex().all { (index, item) -> this[index] == item }
    }

    private fun StringBuilder.createClasses(klasses: List<KlassWithRelations>) {
        klasses.forEach { klass ->
            createClass(klass.item, klass.type)
        }
    }

    private fun StringBuilder.createImports(klasses: List<KlassWithRelations>, childPackages: Collection<String>) {
        val (projectImports, externalImports) = groupAllImports(klasses.flatMap { it.fileImports })
        val currentImports = projectImports.getCurrentImports(childPackages)
        //At this point, the openPackages stack matches the currentImports list
        // Create imported classes in open package tree:
        var lastOpenPackage = openPackages.lastOrNull()
        for (i in currentImports.size - 1 downTo 0) {
            createImportedClassesInPackage(currentImports[i], lastOpenPackage)
            lastOpenPackage = openPackages.last()
            closePackage()
        }

        //Remaining imports:
        createImportedClassesInPackage(externalImports, lastOpenPackage)
    }

    private fun KlassImport.getCurrentImports(childPackages: Collection<String>): List<KlassImport.Package> {
        val currentImports = collectCurrentImportListFromOpenPackages()
        var qualifiedName: Collection<String> = openPackages.first().split(PACKAGE_SEPARATOR) + openPackages.drop(1).map { it }
        var childPackageImports: List<KlassImport> = childPackages.map {
            KlassImport.Package(
                name = it,
                qualifiedName = qualifiedName + it,
                elements = emptyList()
            )
        }
        val additionalImports = mutableListOf<KlassImport.Package>()
        if(openPackages.size > currentImports.size) {
            for (i in openPackages.size - 1 downTo currentImports.size) {
                val packageImport = KlassImport.Package(
                    openPackages[i],
                    qualifiedName,
                    childPackageImports
                )
                if (qualifiedName.isNotEmpty()) {
                    qualifiedName = qualifiedName.take(qualifiedName.size - 1)
                }
                childPackageImports = listOf(packageImport)
                additionalImports.add(packageImport)
            }
        } else {
            val existingPackages = currentImports.last().elements.map { it.name }
            childPackageImports = childPackageImports.filterNot { existingPackages.contains(it.name)  }
        }

        if (currentImports.isNotEmpty()) {
            var replacementImportChild = currentImports[currentImports.size - 1].let { import ->
                import.copy(
                    elements = import.elements.plus(childPackageImports)
                )
            }
            currentImports[currentImports.size - 1] = replacementImportChild
            for (i in currentImports.size - 2 downTo 0) {
                replacementImportChild = currentImports[i].let { import ->
                    val replacementElements = import.elements.toMutableList()
                    val childIndex = replacementElements.indexOf(replacementImportChild)
                    if (childIndex < 0) {
                        throw IllegalStateException(
                            "Package ${replacementImportChild.name} was not found in ${import.name} while building import list!"
                        )
                    }
                    replacementElements[childIndex] = replacementImportChild
                    import.copy(
                        elements = replacementElements
                    )
                }
                currentImports[i] = replacementImportChild
            }
        }
        additionalImports.reverse()
        currentImports.addAll(additionalImports)
        return currentImports
    }

    private fun KlassImport.collectCurrentImportListFromOpenPackages(): MutableList<KlassImport.Package> {
        val currentImports = mutableListOf<KlassImport.Package>()
        var previousImports = this
        for (openPackage in openPackages) {
            val openPackageImports = (previousImports as? KlassImport.Package)?.elements?.firstOrNull { it.name == openPackage } as? KlassImport.Package
            if(openPackageImports == null) {
                //println("Open Package \"$openPackage\" did not exist in klass import structure! Consider a fix?")
                break //TODO: SHOULD THIS EVER HAPPEN?
            }
            currentImports.add(openPackageImports)
            previousImports = openPackageImports
        }
        return currentImports
    }

    private fun groupAllImports(imports: List<KlassItem>): Pair<KlassImport.Package, KlassImport.Package> {
        if (imports.isEmpty()) return KlassImport.Package("", emptyList(), emptyList()) to KlassImport.Package("", emptyList(), emptyList())
        val projectImports = ArrayList<KlassItem>(imports.size)
        val externalImports = ArrayList<KlassItem>(imports.size)
        imports.forEach { input ->
            if (input.filePackage.hasProjectPrefix()) {
                projectImports.add(input.copy(filePackage = input.filePackage.drop(projectBasePrefix.size)))
            } else {
                externalImports.add(input)
            }
        }
        return KlassImport.Package(
            "",
            emptyList(),
            listOf(KlassImport.Package(
                name = generatorSettings.projectBasePrefix,
                qualifiedName = projectBasePrefix,
                elements = groupProjectImportsInner(
                    projectImports,
                    projectBasePrefix
                )
            ))
        ) to KlassImport.Package(
            "",
            emptyList(),
            groupExternalImportsInnerRoot(externalImports)
        )
    }

    private fun groupProjectImportsInner(input: List<KlassItem>, qualifiedName: List<String>): List<KlassImport> {
        return input.groupBy { if (it.filePackage.isEmpty()) null else it.filePackage.first() }
            .map { (key, subLists) ->
                if (key == null) {
                    return@map subLists.distinct().map { KlassImport.Klass(it.name) }
                }
                val nested = subLists.distinct().map { it.copy(filePackage = it.filePackage.drop(1)) }
                val qualifiedNestedName = qualifiedName + key
                val nestedImports = groupProjectImportsInner(nested, qualifiedNestedName)
                listOf(KlassImport.Package(
                    name = key,
                    qualifiedName = qualifiedNestedName,
                    elements = nestedImports
                ))
            }.flatten()
    }

    private fun groupExternalImportsInnerRoot(input: List<KlassItem>): List<KlassImport> {
        return input.map { item -> item to if (item.filePackage.isEmpty()) null else item.filePackage.take(2) }
            .groupBy { (_, filePackage) -> filePackage?.joinToString(PACKAGE_SEPARATOR) }
            .map { (key, subLists) ->
                if (key == null) {
                    return@map subLists.distinct().map { (item, _) -> KlassImport.Klass(item.name) }
                }
                val nested = subLists.distinct().map { (item, _) -> item.copy(filePackage = item.filePackage.drop(2)) }
                val qualifiedName = subLists.firstOrNull()?.second ?: emptyList()
                val nestedImports = groupExternalImportsInnerNested(nested, qualifiedName)
                if (nestedImports.size == 1) {
                    val nestedImport = nestedImports.first()
                    if (nestedImport is KlassImport.Package) {
                        return@map listOf(KlassImport.Package(
                            name = key + nestedImport.name,
                            qualifiedName = qualifiedName + nestedImport.name,
                            elements = nestedImport.elements
                        ))
                    }
                }
                listOf(KlassImport.Package(
                    name = key,
                    qualifiedName = qualifiedName,
                    elements = nestedImports
                ))
            }.flatten()
    }

    private fun groupExternalImportsInnerNested(input: List<KlassItem>, qualifiedName: List<String>): List<KlassImport> {
        return input.groupBy { if (it.filePackage.isEmpty()) null else it.filePackage.first() }
            .map { (key, subLists) ->
                if (key == null) {
                    return@map subLists.distinct().map { KlassImport.Klass(it.name) }
                }
                val nested = subLists.distinct().map { it.copy(filePackage = it.filePackage.drop(1)) }
                val qualifiedNestedName = qualifiedName + key
                val nestedImports = groupExternalImportsInnerNested(nested, qualifiedNestedName)
                if (nestedImports.size == 1) {
                    val nestedImport = nestedImports.first()
                    if (nestedImport is KlassImport.Package) {
                        val name = "${key}.${nestedImport.name}"
                        return@map listOf(KlassImport.Package(
                            name = name,
                            qualifiedName = qualifiedNestedName + nestedImport.name,
                            elements = nestedImport.elements
                        ))
                    }
                }
                listOf(KlassImport.Package(
                    name = key,
                    qualifiedName = qualifiedNestedName,
                    elements = nestedImports
                ))
            }.flatten()
    }

    private fun StringBuilder.createImportedClassesInPackage(
        imports: KlassImport.Package,
        skipPreviousKey: String?
    ) {
        for (element in imports.elements) {
            if (element.name == skipPreviousKey) {
                // Skip already visited key
                continue
            }
            createImportedElement(element)
        }
    }

    private fun StringBuilder.createImportedElement(
        element: KlassImport
    ) {
        when(element) {
            is KlassImport.Package -> {
                beginPackage(element.name, element.qualifiedName)
                for (nestedElement in element.elements) {
                    createImportedElement(nestedElement)
                }
                closePackage()
            }
            is KlassImport.Klass -> {
                createImportedClass(element.name)
            }
        }
    }

    private fun StringBuilder.createImportedClass(name: String) {
        appendContent("interface \"${name}\"")
    }

    private fun StringBuilder.createClass(klassItem: KlassItem, klassType: KlassTypeData) {
        val plantUmlType = when(klassType.type) {
            KlassType.DATA_CLASS -> "entity"
            KlassType.CLASS -> "class"
            KlassType.ABSTRACT_CLASS -> "abstract class"
            KlassType.OBJECT -> "class"
            KlassType.INTERFACE -> "interface"
            KlassType.ENUM_CLASS -> "enum"
            KlassType.UNKNOWN -> "interface"
        }
        appendContent("$plantUmlType \"[[\$pathToSource${klassType.codeBaseName}${PATH_DELIMITER}${klassType.filePath} ${klassItem.name}]]\" as ${klassItem.name} {")
        klassType.methods.forEach { method ->
            appendContent("${" ".repeat(generatorSettings.spaceCount)}{method} $method")
        }
        appendContent("}")
    }

    private fun StringBuilder.beginPackage(name: String, qualifiedName: Collection<String>) {
        val packageName = if (qualifiedName.startsWith(projectPackagePrefix)) {
            if (qualifiedName.size == projectPackagePrefix.size) {
                "[[\$pathToDocsBase${PATH_DELIMITER}${generatorSettings.generatedFileName} $name]]"
            } else {
                val prefixSize = 1 + projectRemainderPrefix.size
                val linkTarget = if (openPackages.size > prefixSize) {
                    "${openPackages.drop(prefixSize).joinToString("$PATH_DELIMITER")}${PATH_DELIMITER}$name"
                } else {
                    name
                }
                "[[\$pathToDocsBase${PATH_DELIMITER}${linkTarget}${PATH_DELIMITER}${generatorSettings.generatedFileName} $name]]"
            }
        } else {
            val externalLink = externalLinks[qualifiedName.joinToString(".")]
            if (externalLink != null) {
                "[[\$pathToRoot${PATH_DELIMITER}$externalLink $name]]"
            } else {
                name
            }
        }
        appendContent("package \"$packageName\" as p\\\$_${packageIndex++} #ffffff {")
        openPackages.push(name)
    }

    private fun StringBuilder.beginSelfPackage(name: String) {
        appendContent("package \"$name\" as p\\\$_${packageIndex++} ${generatorSettings.selfColor} {")
        openPackages.push(name)
    }

    private fun StringBuilder.closePackage() {
        openPackages.pop()
        appendContent("}")
    }


    private fun StringBuilder.createConnections(klasses: List<KlassWithRelations>) {
        klasses.forEach { definition ->
            definition.inheritances.forEach { inheritance ->
                appendContent("${definition.item.name} .up.|> ${inheritance.name}")
            }
            definition.parameters.forEach { parameter ->
                appendContent("${definition.item.name} o-down- ${parameter.name}")
            }
            definition.usages.forEach { parameter ->
                appendContent("${definition.item.name} -down-> ${parameter.name}")
            }
        }
    }

    private fun StringBuilder.appendContent(string: String) {
        append(" ".repeat(openPackages.size * generatorSettings.spaceCount))
        appendLine(string)
    }

    private fun Collection<String>.startsWith(prefix: List<String>): Boolean {
        if (this.size < prefix.size) {
            return false
        }
        prefix.zip(this.take(prefix.size)).forEach { (prefixElement, thisElement) ->
            if (thisElement != prefixElement) {
                return false
            }
        }
        return true
    }

    private companion object {
        private const val PACKAGE_SEPARATOR = "."
        private const val PACKAGE_DELIMITER = '.'
        private const val PATH_DELIMITER = '/'
    }
}
