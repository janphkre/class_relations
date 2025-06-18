package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.KlassItem
import de.janphkre.class_relations.library.model.KlassWithRelations
import de.janphkre.class_relations.library.model.UnclearKlassItem
import java.io.Serializable

class FilteringClassRelationsPumlGeneratorImpl(
    private val delegate: ClassRelationsPumlGenerator
): ClassRelationsPumlGenerator by delegate {

    override fun generate(klasses: List<KlassWithRelations>, childPackages: List<String>, rootGeneratedLink: String): String {
        val filteredKlasses = klasses.mapNotNull { klass ->
            if (klass.item.isDisabled) {
                return@mapNotNull null
            }
            return@mapNotNull klass.copy(
                fileImports = klass.fileImports.filterAndRectifyBy(klasses),
                parameters = klass.parameters.filterAndRectifyBy(klasses),
                methodParameters = klass.methodParameters.filterAndRectifyBy(klasses),
                inheritances = klass.inheritances.filterAndRectifyBy(klasses),
            )
        }
        return delegate.generate(filteredKlasses, childPackages, rootGeneratedLink)
    }

    private fun List<KlassItem>.filterAndRectifyBy(currentKlasses: List<KlassWithRelations>): List<KlassItem> {
        return map { import ->
            if (import !is UnclearKlassItem) {
                return@map import
            }
            val localItem = currentKlasses.firstOrNull { it.item.name == import.name }
            return@map localItem?.item ?: import
        }.filterNot { it.isDisabled }
    }
}