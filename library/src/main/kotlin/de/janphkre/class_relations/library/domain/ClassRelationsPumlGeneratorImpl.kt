package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.*
import java.util.*

internal class ClassRelationsPumlGeneratorImpl(
    private val generatorSettings: ClassRelationsPumlGenerator.Settings
) : ClassRelationsPumlGenerator {

    private val projectPrefix = generatorSettings.projectPackagePrefix.split('.')
    private val openPackages = Stack<String>()
    private var packageIndex = 0

    override fun generate(klasses: List<KlassWithRelations>, rootGeneratedLink: String): String {
        packageIndex = 0
        return StringBuilder(generatorSettings.initialCapacitySize * klasses.size).apply {
            appendContent("@startuml")
            //STRUCTURE
            createPackageStructure(klasses, rootGeneratedLink)
            createClasses(klasses)
            createImports(klasses, rootGeneratedLink)
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

    private fun StringBuilder.createPackageStructure(klasses: List<KlassWithRelations>, rootGeneratedLink: String) {
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

    private fun StringBuilder.createImports(klasses: List<KlassWithRelations>, rootGeneratedLink: String) {
        val (projectImports, externalImports) = groupAllImports(klasses.flatMap { it.fileImports })
        //CurrentPackage tree first:
        val currentImports = mutableListOf<KlassImport.Package>()
        var previousImports = projectImports
        for (openPackage in openPackages) {
            val openPackageImports = (previousImports as? KlassImport.Package)?.elements?.firstOrNull { it.name == openPackage } as? KlassImport.Package
            if(openPackageImports == null) {
                break
            }
            currentImports.add(openPackageImports)
            previousImports = openPackageImports
        }

        // Close packages that do not have used classes:
        for (i in openPackages.size - 1 downTo currentImports.size ) {
            closePackage()
        }
        // Create imported classes in open package tree:
        var lastOpenPackage = openPackages.lastOrNull()
        for (i in currentImports.size - 1 downTo 0) {
            createImportedClassesInPackage(currentImports[i], lastOpenPackage, "$rootGeneratedLink/${projectPrefix.joinToString("/")}")
            lastOpenPackage = openPackages.last()
            closePackage()
        }

        //Remaining imports:
        createImportedClassesInPackage(externalImports, lastOpenPackage, null)
    }

    private fun groupAllImports(imports: List<KlassItem>): Pair<KlassImport.Package, KlassImport.Package> {
        if (imports.isEmpty()) return KlassImport.Package("", emptyList()) to KlassImport.Package("", emptyList())
        val projectImports = ArrayList<KlassItem>(imports.size)
        val externalImports = ArrayList<KlassItem>(imports.size)
        imports.forEach { input ->
            if (input.filePackage.hasProjectPrefix()) {
                projectImports.add(KlassItem(input.name, input.filePackage.drop(projectPrefix.size)))
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
                val nested = subLists.distinct().map { KlassItem(it.name, it.filePackage.drop(1)) }
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
                val nested = subLists.distinct().map { KlassItem(it.name, it.filePackage.drop(1)) }
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