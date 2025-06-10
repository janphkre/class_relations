package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.KlassWithRelations

class FilteringKotlinParser(
    private val delegate: KotlinParser
): KotlinParser {
    override fun parse(fileContent: String, presentableName: String, filePath: String): KlassWithRelations? {
        val delegateResult = delegate.parse(fileContent, presentableName, filePath) ?: return null
        if (delegateResult.item.isDisabled) {
            return null
        }
        return delegateResult.copy(
            fileImports = delegateResult.fileImports.filterNot { it.isDisabled },
            parameters = delegateResult.parameters.filterNot { it.isDisabled },
            methodParameters = delegateResult.methodParameters.filterNot { it.isDisabled },
            inheritances = delegateResult.inheritances.filterNot { it.isDisabled },
        )
    }
}