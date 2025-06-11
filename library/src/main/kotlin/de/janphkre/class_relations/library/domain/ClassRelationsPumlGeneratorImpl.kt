package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.*
import java.util.*

internal class ClassRelationsPumlGeneratorImpl(
    private val generatorSettings: ClassRelationsPumlGenerator.Settings
) : ClassRelationsPumlGenerator {

    private val projectPrefix = generatorSettings.projectPackagePrefix.split('.')
    private val openPackages = Stack<String>()
    private var packageIndex = 0

    override fun generate(klasses: List<KlassWithRelations>, childPackages: List<String>, rootGeneratedLink: String): String {
        packageIndex = 0
        return StringBuilder(generatorSettings.initialCapacitySize * klasses.size).apply {
            appendContent("@startuml")
            //STRUCTURE
            createSelfPackageStructure(klasses, rootGeneratedLink)
            createClasses(klasses)
            createImports(klasses, childPackages, rootGeneratedLink)
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

    private fun StringBuilder.createSelfPackageStructure(klasses: List<KlassWithRelations>, rootGeneratedLink: String) {
        val anyKlass = klasses.first()
        val pathDepth = anyKlass.item.filePackage.size + rootGeneratedLink.count { it == '/' || it == '\\' }
        appendContent("!\$pathToBase = \"${"..".repeat(pathDepth, '/')}\"")
        if (!anyKlass.item.filePackage.hasProjectPrefix()) {
            beginSelfPackage(anyKlass.item.filePackage.joinToString("."))
            return
        }
        if (projectPrefix.size == anyKlass.item.filePackage.size) {
            beginSelfPackage(anyKlass.item.filePackage.joinToString("."))
            return
        }
        beginPackage(generatorSettings.projectPackagePrefix, rootGeneratedLink)
        val packages = anyKlass.item.filePackage.drop(projectPrefix.size)
        var packageLink = rootGeneratedLink
        for (i in 0 until packages.size - 1) {
            packageLink += "/${packages[i]}"
            beginPackage(packages[i], packageLink)
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

    private fun StringBuilder.createImports(klasses: List<KlassWithRelations>, childPackages: List<String>, rootGeneratedLink: String) {
        val (projectImports, externalImports) = groupAllImports(klasses.flatMap { it.fileImports })
        val currentImports = projectImports.getCurrentImports(childPackages)

        println("Current imports are ${currentImports.joinToString()}")

        // Close packages that do not have used classes:
        assert(openPackages.size == currentImports.size) //TODO?
        // Create imported classes in open package tree:
        var lastOpenPackage = openPackages.lastOrNull()
        println("Continuing with openPackage: $lastOpenPackage")
        for (i in currentImports.size - 1 downTo 0) {
            val projectRootGeneratedLink = "$rootGeneratedLink/${projectPrefix.joinToString("/")}"
            val currentPackageRootGeneratedLink = if (openPackages.size <= 1) {
                projectRootGeneratedLink
            } else {
                "$projectRootGeneratedLink/${openPackages.drop(1).joinToString("/")}"
            }
            createImportedClassesInPackage(currentImports[i], lastOpenPackage, currentPackageRootGeneratedLink)
            lastOpenPackage = openPackages.last()
            closePackage()
        }

        //Remaining imports:
        createImportedClassesInPackage(externalImports, lastOpenPackage, null)
    }

    private fun KlassImport.getCurrentImports(childPackages: List<String>): List<KlassImport.Package> {
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
        return KlassImport.Package("", listOf(KlassImport.Package(generatorSettings.projectPackagePrefix, groupProjectImportsInner(projectImports)))) to KlassImport.Package("", groupExternalImportsInner(externalImports))
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
        skipPreviousKey: String?,
        rootGeneratedLink: String?
    ) {
        for (element in imports.elements) {
            if (element.name == skipPreviousKey) {
                // Skip already visited key
                continue
            }
            createImportedElement(element, rootGeneratedLink)
        }
    }

    private fun StringBuilder.createImportedElement(
        element: KlassImport,
        rootGeneratedLink: String?
    ) {
        println("CREATING imported elements: $element in $rootGeneratedLink")
        when(element) {
            is KlassImport.Package -> {
                val packageTarget = if (rootGeneratedLink == null) {
                    null
                } else {
                    "${rootGeneratedLink}/${element.name}"
                }
                beginPackage(element.name, packageTarget)
                for (nestedElement in element.elements) {
                    createImportedElement(nestedElement, rootGeneratedLink)
                }
                closePackage()
            }
            is KlassImport.Klass -> {
                createImportedClass(element.name)
            }
        }
    }

    private fun StringBuilder.createImportedClass(name: String) {
        appendContent("circle \"${name}\"")
    }

    private fun StringBuilder.createClass(klassItem: KlassItem, klassType: KlassTypeData) {
        val plantUmlType = when(klassType.type) {
            KlassType.DATA_CLASS -> "entity"
            KlassType.CLASS -> "class"
            KlassType.ABSTRACT_CLASS -> "abstract class"
            KlassType.OBJECT -> "class"
            KlassType.INTERFACE -> "interface"
            KlassType.ENUM_CLASS -> "enum"
            KlassType.UNKNOWN -> "diamond"
        }
        appendContent("$plantUmlType \"[[\$pathToBase/${klassType.filePath} ${klassItem.name}]]\" as ${klassItem.name} {")
        klassType.methods.forEach { method ->
            appendContent("${" ".repeat(generatorSettings.spaceCount)}{method} $method")
        }
        appendContent("}")
    }

    private fun StringBuilder.beginPackage(name: String, linkTarget: String?) {
        if (linkTarget != null) {
            appendContent("package \"[[\$pathToBase/$linkTarget/${generatorSettings.generatedFileName} $name]]\" as p\\\$_${packageIndex++} #ffffff {")
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
}