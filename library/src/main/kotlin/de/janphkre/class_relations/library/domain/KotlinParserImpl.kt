package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.*
import kotlinx.ast.common.AstFailure
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.AstSuccess
import kotlinx.ast.common.ast.DefaultAstNode
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.common.summary.Import
import kotlinx.ast.grammar.kotlin.common.summary.PackageHeader
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser

/**
 * Grabs package name, imports and primary constructor parameters from file.
 */
internal object KotlinParserImpl: KotlinParser {
    override fun parse(fileContent: String, presentableName: String, filePath: String): KlassWithRelations? {
        val kotlinFileSummary = KotlinGrammarAntlrKotlinParser.parseKotlinFile(AstSource.String(description = presentableName, content = fileContent)).summary(attachRawAst = false)
        if (kotlinFileSummary is AstFailure) {
            kotlinFileSummary.errors.forEach(::println)
            return null
        }
        kotlinFileSummary as AstSuccess
        val packageHeader: PackageHeader? = kotlinFileSummary.success.find { it is PackageHeader } as? PackageHeader
        val klassDeclaration: KlassDeclaration? = kotlinFileSummary.success.find { it is KlassDeclaration } as? KlassDeclaration
        val importList: DefaultAstNode? = kotlinFileSummary.success.find { it is DefaultAstNode } as? DefaultAstNode

        val filePackage = packageHeader?.identifier?.map { it.identifier } ?: emptyList()
        val fileImportItems = importList?.children?.map { import -> (import as Import).identifier.map { it.identifier } }?.distinct()?.map { KlassItem(it.last(), it.dropLast(1)) } ?: emptyList()
        return KlassWithRelations(
            item = KlassItemWithType(
                name = klassDeclaration?.identifier?.identifier ?: presentableName,
                filePackage = filePackage,
                type = klassDeclaration?.keyword?.let { keyword -> KlassType.values().firstOrNull { it.id == keyword } } ?: KlassType.UNKNOWN,
                methods = (klassDeclaration?.expressions?.find { it.description == "classBody" } as? DefaultAstNode)?.children?.mapNotNull { (it as? KlassDeclaration)?.identifier?.identifier } ?: emptyList(),
                filePath = filePath
            ),
            fileImports = fileImportItems,
            parameters = klassDeclaration?.parameter?.flatMap { parameter -> parameter.parameter.flatMap { type -> type.type.map { it.identifier } } }?.distinct()?.map { parameter -> fileImportItems.firstOrNull { it.name == parameter } ?: KlassItem(parameter, filePackage) } ?: emptyList(), //TODO: SUPPORT ALIAS IMPORTS
            inheritances = klassDeclaration?.inheritance?.map { it.type.identifier }?.map { inheritance -> fileImportItems.firstOrNull { it.name == inheritance } ?: KlassItem(inheritance, filePackage) } ?: emptyList(), //TODO: SUPPORT ALIAS IMPORTS,
        )
    }
}