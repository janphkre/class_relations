package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.KlassDefinition

interface KotlinParser {

    fun parse(fileContent: String, presentableName: String, filePath: String): KlassDefinition?

    companion object {
        fun getInstance(): KotlinParser {
            return KotlinParserImpl
        }
    }
}