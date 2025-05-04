package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.data.KlassItemFactory
import de.janphkre.class_relations.library.model.KlassWithRelations

interface KotlinParser {

    fun parse(fileContent: String, presentableName: String, filePath: String): KlassWithRelations?

    companion object {
        fun getInstance(): KotlinParser {
            return KotlinParserImpl(KlassItemFactory.getInstance())
        }
    }
}