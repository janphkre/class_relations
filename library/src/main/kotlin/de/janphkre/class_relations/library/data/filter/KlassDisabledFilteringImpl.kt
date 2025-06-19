package de.janphkre.class_relations.library.data.filter

import de.janphkre.class_relations.library.model.KlassItem
import de.janphkre.class_relations.library.model.KlassWithRelations
import de.janphkre.class_relations.library.model.UnclearKlassItem

class KlassDisabledFilteringImpl: KlassDisabledFiltering {
    override fun filter(klasses: List<KlassWithRelations>): List<KlassWithRelations> {
        return klasses.mapNotNull { klass ->
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