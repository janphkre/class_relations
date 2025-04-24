package de.janphkre.class_relations.library.model

sealed interface KlassImport {
    val name: String

    data class Package(
        override val name: String,
        val elements: List<KlassImport>
    ) : KlassImport

    class Klass(
        override val name: String,
    ) : KlassImport
}