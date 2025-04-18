package de.janphkre.class_relations_viewer.domain

import com.intellij.openapi.vfs.VirtualFile
import de.janphkre.class_relations_viewer.model.KlassDefinition
import de.janphkre.class_relations_viewer.model.KlassType
import kotlinx.ast.common.AstFailure
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.AstSuccess
import kotlinx.ast.common.ast.*
import kotlinx.ast.common.klass.Klass
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.common.summary.Import
import kotlinx.ast.grammar.kotlin.common.summary.PackageHeader
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser

/**
 * Grabs package name, imports and primary constructor parameters from file.
 */
class KotlinHeaderParser {
    fun parse(file: VirtualFile): KlassDefinition? {
        val kotlinFileSummary = KotlinGrammarAntlrKotlinParser.parseKotlinFile(AstSource.File(file.path)).summary(attachRawAst = false)
        if (kotlinFileSummary is AstFailure) {
            kotlinFileSummary.errors.forEach(::println)
            return null
        }
        kotlinFileSummary as AstSuccess
        val packageHeader: PackageHeader? = kotlinFileSummary.success.find { it is PackageHeader } as? PackageHeader
        val klassDeclaration: KlassDeclaration? = kotlinFileSummary.success.find { it is KlassDeclaration } as? KlassDeclaration
        val importList: DefaultAstNode? = kotlinFileSummary.success.find { it is DefaultAstNode } as? DefaultAstNode
        return KlassDefinition(
            name = klassDeclaration?.identifier?.identifier ?: file.presentableName,
            filePackage = packageHeader?.identifier?.map { it.identifier } ?: emptyList(),
            classType = klassDeclaration?.keyword?.let { keyword -> KlassType.values().firstOrNull { it.id == keyword } } ?: KlassType.UNKNOWN,
            fileImports = importList?.children?.map { import -> (import as Import).identifier.map { it.identifier } } ?: emptyList(),
            parameters = klassDeclaration?.parameter?.flatMap { parameter -> parameter.parameter.flatMap { type -> type.type.map { it.identifier } } } ?: emptyList(),
            inheritances = klassDeclaration?.inheritance?.map { it.type.identifier } ?: emptyList(),
            methods = (klassDeclaration?.expressions?.find { it.description == "classBody" } as? DefaultAstNode)?.children?.mapNotNull { (it as? KlassDeclaration)?.identifier?.identifier } ?: emptyList(),
        )
    }
}