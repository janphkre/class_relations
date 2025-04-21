package de.janphkre.class_relations_viewer.library.domain

import de.janphkre.class_relations_viewer.library.model.*
import java.util.*

class ClassRelationsPumlGenerator(
    private val generatorSettings: Settings
) {

    data class Settings(
        val projectPackagePrefix: String,
        val selfColor: String,
        val spaceCount: Int,
        val generatedFileName: String,
        val initialCapacitySize : Int = 2048
    )

    private val openPackages = Stack<String>()

    // All klasses must belong to the same package!
    fun generate(klasses: List<KlassWithRelations>, rootGeneratedLink: String): String {
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
        val projectPrefix = generatorSettings.projectPackagePrefix.split('.')
        val anyKlass = klasses.first()
        if (!projectPrefix.withIndex().all { (index, item) -> anyKlass.item.filePackage[index] == item }) {
            beginSelfPackage(anyKlass.item.filePackage.joinToString("."))
        } else {
            val packages = anyKlass.item.filePackage.drop(projectPrefix.size)
            if (packages.size > 1) {
                beginPackage(generatorSettings.projectPackagePrefix, rootGeneratedLink)
                var packageLink = rootGeneratedLink
                for (i in 0 until packages.size - 1) {
                    packageLink += "/${packages[i]}"
                    beginPackage(packages[i], packageLink)
                }
                beginSelfPackage(packages.last())
            } else {
                beginSelfPackage(generatorSettings.projectPackagePrefix)
            }
        }
    }

    private fun StringBuilder.createClasses(klasses: List<KlassWithRelations>) {
        klasses.forEach { klass ->
            createClass(klass.item)
        }
    }

    private fun StringBuilder.createImports(klasses: List<KlassWithRelations>, rootGeneratedLink: String) {
        val imports = groupImports(klasses.flatMap { it.fileImports })
        //CurrentPackage tree first:
        val currentImports = mutableListOf<KlassImport.Package>()
        var previousImports = imports
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
            createImportedClassesInPackage(currentImports[i], lastOpenPackage, rootGeneratedLink)
            lastOpenPackage = openPackages.last()
            closePackage()
        }

        //Remaining imports:
        createImportedClassesInPackage(imports, lastOpenPackage, rootGeneratedLink)
    }

    private fun groupImports(input: List<KlassItem>): KlassImport.Package {
        if (input.isEmpty()) return KlassImport.Package("", emptyList())
        return KlassImport.Package("", groupImportsInner(input))
    }

    private fun groupImportsInner(input: List<KlassItem>):  List<KlassImport> {
        return input.groupBy { if (it.filePackage.isEmpty()) null else it.filePackage.first() }
            .map { (key, subLists) ->
                if (key == null) {
                    return@map subLists.map { KlassImport.Klass(it.name) }
                }
                val nested = subLists.map { KlassItem(it.name, it.filePackage.drop(1)) }
                listOf(KlassImport.Package(key, groupImportsInner(nested)))
            }.flatten()
    }

    private fun StringBuilder.createImportedClassesInPackage(
        imports: KlassImport.Package,
        skipPreviousKey: String?,
        rootGeneratedLink: String
    ) {
        for (element in imports.elements) {
            if (element.name == skipPreviousKey) {
                // Skip already visited key
                continue
            }
            when(element) {
                is KlassImport.Package -> {
                    val packageTarget = "${rootGeneratedLink}/${element.name}"
                    beginPackage(element.name, packageTarget)
                    createImportedClassesInPackage(element, packageTarget)
                    closePackage()
                }
                is KlassImport.Klass -> {
                    createImportedClass(element.name)
                }
            }
        }
    }

    private fun StringBuilder.createImportedClassesInPackage(
        imports: KlassImport.Package,
        rootGeneratedLink: String
    ) {
        for (element in imports.elements) {
            when(element) {
                is KlassImport.Package -> {
                    val packageTarget = "${rootGeneratedLink}/${element.name}"
                    beginPackage(element.name, packageTarget)
                    createImportedClassesInPackage(element, packageTarget)
                    closePackage()
                }
                is KlassImport.Klass -> {
                    createImportedClass(element.name)
                }
            }
        }
    }

    private fun StringBuilder.createImportedClass(name: String) {
        appendContent("circle \"${name}\"")
    }

    private fun StringBuilder.createClass(klassItem: KlassItemWithType) {
        val plantUmlType = when(klassItem.type) {
            KlassType.DATA_CLASS -> "entity"
            KlassType.CLASS -> "class"
            KlassType.ABSTRACT_CLASS -> "abstract class"
            KlassType.OBJECT -> "class"
            KlassType.INTERFACE -> "interface"
            KlassType.ENUM_CLASS -> "enum"
            KlassType.UNKNOWN -> "diamond"
        }
        appendContent("$plantUmlType \"[[${klassItem.filePath} ${klassItem.name}]]\" as ${klassItem.name} {")
        klassItem.methods.forEach { method ->
            appendContent("${" ".repeat(generatorSettings.spaceCount)}{method} $method")
        }
        appendContent("}")
    }

    private fun StringBuilder.beginPackage(name: String, linkTarget: String?) {
        if (linkTarget != null) {
            appendContent("package \"[[$linkTarget/$${generatorSettings.generatedFileName} $name]]\" #ffffff {")
        } else {
            appendContent("package \"$name\" #ffffff {")
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