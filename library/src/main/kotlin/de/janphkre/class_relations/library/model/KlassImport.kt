package de.janphkre.class_relations.library.model

sealed interface KlassImport {
    val name: String

    data class Package(
        override val name: String,
        val elements: List<KlassImport>
    ) : KlassImport {
        override fun hashCode(): Int {
            return name.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return name == ((other as? Package)?.name ?: return false)
        }
    }

    class Klass(
        override val name: String,
    ) : KlassImport
}