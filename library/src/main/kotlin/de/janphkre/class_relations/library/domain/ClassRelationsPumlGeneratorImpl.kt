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
    private val generatorSettings: ClassRelationsPumlGenerator.Settings
) : ClassRelationsPumlGenerator {

    private val projectPrefix = generatorSettings.projectPackagePrefix.split('.')
    private val openPackages = Stack<String>()
    private var packageIndex = 0

    override fun generate(klasses: List<KlassWithRelations>, childPackages: Collection<String>, sourcesLinks: Map<String, String>): String {
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
        childPackages: Collection<String>,
        sourcesLink: String
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
        for ((codeBaseName, link) in sourcesLinks) {
            appendContent("!\$pathTo${codeBaseName} = \"${filePathReverse}${PATH_DELIMITER}${link}\"")
        }
        appendContent("!\$pathToDocsBase = \"${filePathReverse}${PATH_DELIMITER}${generatorSettings.projectPackagePrefix.replace(PACKAGE_DELIMITER, PATH_DELIMITER)}\"")
    }

    private fun StringBuilder.createSelfPackageStructure(filePackage: List<String>) {
        if (!filePackage.hasProjectPrefix()) {
            beginSelfPackage(filePackage.joinToString(PACKAGE_SEPARATOR))
            return
        }
        if (projectPrefix.size == filePackage.size) {
            beginSelfPackage(filePackage.joinToString(PACKAGE_SEPARATOR))
            return
        }
        beginPackage(generatorSettings.projectPackagePrefix)
        val packages = filePackage.drop(projectPrefix.size)
        var packageLink = ""
        for (i in 0 until packages.size - 1) {
            packageLink += "/${packages[i]}"
            beginPackage(packages[i])
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
        if (size < projectPrefix.size) {
            return false
        }
        return projectPrefix.withIndex().all { (index, item) -> this[index] == item }
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
        val currentImports = mutableListOf<KlassImport.Package>()
        var previousImports = this
        for (openPackage in openPackages) {
            val openPackageImports = (previousImports as? KlassImport.Package)?.elements?.firstOrNull { it.name == openPackage } as? KlassImport.Package
            if(openPackageImports == null) {
                break
            }
            currentImports.add(openPackageImports)
            previousImports = openPackageImports
        }

        var childPackageImports: List<KlassImport> = childPackages.map { KlassImport.Package(it, emptyList()) }
        val additionalImports = mutableListOf<KlassImport.Package>()
        if(openPackages.size > currentImports.size) {
            for (i in openPackages.size - 1 downTo currentImports.size) {
                val packageImport = KlassImport.Package(
                    openPackages[i],
                    childPackageImports
                )
                childPackageImports = listOf(packageImport)
                additionalImports.add(packageImport)
            }
        } else {
            val existingPackages = currentImports.last().elements.map { it.name }
            childPackageImports = childPackageImports.filterNot { existingPackages.contains(it.name)  }
        }

        if (currentImports.size > 0) {
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

    private fun groupAllImports(imports: List<KlassItem>): Pair<KlassImport.Package, KlassImport.Package> {
        if (imports.isEmpty()) return KlassImport.Package("", emptyList()) to KlassImport.Package("", emptyList())
        val projectImports = ArrayList<KlassItem>(imports.size)
        val externalImports = ArrayList<KlassItem>(imports.size)
        imports.forEach { input ->
            if (input.filePackage.hasProjectPrefix()) {
                projectImports.add(input.copy(filePackage = input.filePackage.drop(projectPrefix.size)))
            } else {
                externalImports.add(input)
            }
        }
        return KlassImport.Package(
            "",
            listOf(KlassImport.Package(generatorSettings.projectPackagePrefix, groupProjectImportsInner(projectImports)))
        ) to KlassImport.Package(
            "",
            groupExternalImportsInner(externalImports)
        )
    }

    private fun groupProjectImportsInner(input: List<KlassItem>): List<KlassImport> {
        return input.groupBy { if (it.filePackage.isEmpty()) null else it.filePackage.first() }
            .map { (key, subLists) ->
                if (key == null) {
                    return@map subLists.distinct().map { KlassImport.Klass(it.name) }
                }
                val nested = subLists.distinct().map { it.copy(filePackage = it.filePackage.drop(1)) }
                val nestedImports = groupProjectImportsInner(nested)
                listOf(KlassImport.Package(key, nestedImports))
            }.flatten()
    }

    private fun groupExternalImportsInner(input: List<KlassItem>): List<KlassImport> {
        return input.groupBy { if (it.filePackage.isEmpty()) null else it.filePackage.first() }
            .map { (key, subLists) ->
                if (key == null) {
                    return@map subLists.distinct().map { KlassImport.Klass(it.name) }
                }
                val nested = subLists.distinct().map { it.copy(filePackage = it.filePackage.drop(1)) }
                val nestedImports = groupExternalImportsInner(nested)
                if (nestedImports.size == 1) {
                    val nestedImport = nestedImports.first()
                    if (nestedImport is KlassImport.Package) {
                        return@map listOf(KlassImport.Package("${key}.${nestedImport.name}", nestedImport.elements))
                    }
                }
                listOf(KlassImport.Package(key, nestedImports))
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
                beginPackage(element.name)
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
        appendContent("$plantUmlType \"[[\$pathTo${klassType.codeBaseName}${PATH_DELIMITER}${klassType.filePath} ${klassItem.name}]]\" as ${klassItem.name} {")
        klassType.methods.forEach { method ->
            appendContent("${" ".repeat(generatorSettings.spaceCount)}{method} $method")
        }
        appendContent("}")
    }

    private fun StringBuilder.beginPackage(name: String) {
        if (openPackages.firstOrNull() == generatorSettings.projectPackagePrefix) {
            val linkTarget = if (openPackages.size > 1) {
                "${openPackages.drop(1).joinToString("/")}/$name"
            } else {
                name
            }
            appendContent("package \"[[\$pathToDocsBase${PATH_DELIMITER}${linkTarget}${PATH_DELIMITER}${generatorSettings.generatedFileName} $name]]\" as p\\\$_${packageIndex++} #ffffff {")
        } else if (name == generatorSettings.projectPackagePrefix) {
            appendContent("package \"[[\$pathToDocsBase${PATH_DELIMITER}${generatorSettings.generatedFileName} $name]]\" as p\\\$_${packageIndex++} #ffffff {")
        } else {
            appendContent("package \"$name\" as p\\\$_${packageIndex++} #ffffff {")
        }
        openPackages.push(name)
    }

    private fun StringBuilder.beginSelfPackage(name: String) {
        appendContent("package \"$name\" ${generatorSettings.selfColor} {")
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

    private companion object {
        private const val PACKAGE_SEPARATOR = "."
        private const val PACKAGE_DELIMITER = '.'
        private const val PATH_DELIMITER = '/'
    }
}