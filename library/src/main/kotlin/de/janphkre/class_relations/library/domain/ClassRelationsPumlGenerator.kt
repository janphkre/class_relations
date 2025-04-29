package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.KlassWithRelations
import java.io.Serializable

interface ClassRelationsPumlGenerator {

    data class Settings(
        val projectPackagePrefix: String,
        val selfColor: String,
        val spaceCount: Int,
        val generatedFileName: String,
        val initialCapacitySize : Int = 2048
    ): Serializable

    // All klasses must belong to the same package / folder!
    fun generate(klasses: List<KlassWithRelations>, rootGeneratedLink: String): String

    companion object {
        fun getInstance(settings: Settings): ClassRelationsPumlGenerator {
            return ClassRelationsPumlGeneratorImpl(
                settings
            )
        }
    }
}