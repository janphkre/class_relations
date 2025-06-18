package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.data.item.KlassItemFactory
import de.janphkre.class_relations.library.model.*
import kotlinx.ast.common.AstFailure
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.AstSuccess
import kotlinx.ast.common.ast.AstAttachmentRawAst
import kotlinx.ast.common.ast.DefaultAstNode
import kotlinx.ast.common.ast.DefaultAstTerminal
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.common.klass.RawAst
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.common.summary.Import
import kotlinx.ast.grammar.kotlin.common.summary.PackageHeader
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser

/**
 * Grabs package name, imports and primary constructor parameters from file.
 */
internal class KotlinParserImpl(
    private val klassItemFactory: KlassItemFactory
): KotlinParser {

    override fun parse(fileContent: String, presentableName: String, filePath: String): KlassWithRelations? {
        val kotlinFileSummary = KotlinGrammarAntlrKotlinParser.parseKotlinFile(AstSource.String(description = presentableName, content = fileContent)).summary(false)
        if (kotlinFileSummary is AstFailure) {
            kotlinFileSummary.errors.forEach(::println)
            return null
        }
        kotlinFileSummary as AstSuccess
        val packageHeader: PackageHeader? = kotlinFileSummary.success.find { it is PackageHeader } as? PackageHeader
        val klassDeclaration: KlassDeclaration? = kotlinFileSummary.success.find { it is KlassDeclaration } as? KlassDeclaration
        val importList: DefaultAstNode? = kotlinFileSummary.success.find { it is DefaultAstNode } as? DefaultAstNode

        val filePackage = packageHeader?.identifier?.map { it.identifier } ?: emptyList()
        val fileImportItems = importList?.children?.map { import -> (import as Import).identifier.map { it.identifier } }?.distinct()?.map { klassItemFactory.createItem(it.last(), it.dropLast(1)) } ?: emptyList()
        val methods = (klassDeclaration?.expressions?.find { it.description == "classBody" } as? DefaultAstNode)?.children?.filterIsInstance<KlassDeclaration>()
        return KlassWithRelations(
            item = klassItemFactory.createItem(
                name = klassDeclaration?.identifier?.identifier ?: presentableName,
                packageList = filePackage
            ),
            type = KlassTypeData(
                type = klassDeclaration?.getType() ?: KlassType.UNKNOWN,
                methods = methods?.mapNotNull { it.identifier?.identifier } ?: emptyList(),
                filePath = filePath
            ),
            fileImports = fileImportItems,
            parameters = klassDeclaration?.getParameters(fileImportItems) ?: emptyList(), //TODO: SUPPORT ALIAS IMPORTS
            inheritances = klassDeclaration?.getInheritances(fileImportItems) ?: emptyList(), //TODO: SUPPORT ALIAS IMPORTS,
            methodParameters = methods?.getMethodParameters(fileImportItems) ?: emptyList()
        )
    }

    private fun KlassDeclaration.getType(): KlassType? {
        val modifier = modifiers.firstOrNull { it.group.group != "visibilityModifier" }?.modifier
        val id = if (modifier != null) {
            "$modifier $keyword"
        } else {
            keyword
        }
        return KlassType.values().firstOrNull { it.id == id }
    }

    private fun KlassDeclaration.getParameters(fileImportItems: List<KlassItem>): List<KlassItem> {
        return parameter.flatMap { parameter -> parameter.parameter.flatMap { type -> type.type.map { it.identifier } } }
            .distinct()
            .map { parameter ->
                fileImportItems.firstOrNull { it.name == parameter } ?: klassItemFactory.createItem(parameter)
            }
    }

    private fun KlassDeclaration.getInheritances(fileImportItems: List<KlassItem>): List<KlassItem> {
        return inheritance.map { it.type.identifier }
            .map { inheritance ->
                fileImportItems.firstOrNull { it.name == inheritance } ?: klassItemFactory.createItem(inheritance)
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun List<KlassDeclaration>.getMethodParameters(fileImportItems: List<KlassItem>): List<KlassItem> {
        return this.flatMap { method ->
            val ast = (method.attachments.attachments[AstAttachmentRawAst] as? RawAst)?.ast as? DefaultAstNode
                ?: return@flatMap emptyList<KlassItem>()
            return@flatMap ast.children.flatMap { methodPart ->
                when (methodPart.description) {
                    "functionValueParameters" -> {
                        (methodPart as DefaultAstNode).children.flatMap functionParameter@{ functionParameter ->
                            if (functionParameter.description != "functionValueParameter") {
                                return@functionParameter emptyList<KlassItem>()
                            }
                            val typeNode = (((functionParameter as DefaultAstNode)
                                .children.first { it.description == "parameter" } as DefaultAstNode)
                                .children.last { it.description == "type" } as DefaultAstNode)
                            var children = typeNode.children
                            var typeReferences = children.filter { it.description == "typeReference" } as List<DefaultAstNode>
                            if (typeReferences.isEmpty()) {
                                while (true) {
                                    val nestedChildren = (children.filter {
                                        it.description == "nullableType" ||
                                                it.description == "parenthesizedType" ||
                                                it.description == "functionType" ||
                                                it.description == "type" ||
                                                it.description == "functionTypeParameters"
                                    } as List<DefaultAstNode>)
                                        .flatMap { it.children }
                                    children =
                                        nestedChildren.plus(children.filter { it.description == "typeReference" })
                                    if (nestedChildren.isEmpty()) {
                                        typeReferences = children as List<DefaultAstNode>
                                        break
                                    }
                                }
                            }
                            val classNames = typeReferences.map { reference ->
                                ((((reference.children.first { it.description == "userType" } as DefaultAstNode)
                                    .children.first { it.description == "simpleUserType" } as DefaultAstNode)
                                    .children.first { it.description == "simpleIdentifier" } as DefaultAstNode)
                                    .children.first() as DefaultAstTerminal).text
                            }
                            return@functionParameter classNames.map { className ->
                                fileImportItems.firstOrNull { it.name == className }
                                    ?: klassItemFactory.createItem(className)
                            }
                        }
                    }
                    else -> {
                        emptyList<KlassItem>()
                    }
                }
            }
        }
    }
}
