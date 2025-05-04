package de.janphkre.class_relations.library.domain.filter

import de.janphkre.class_relations.library.data.KlassItemFactory
import de.janphkre.class_relations.library.model.KlassItemFilter


class ExactKlassFilter(
    private val classesToBeFiltered: Collection<KlassItemFilter>,
    private val factory: KlassItemFactory
): KlassFilter {
    override fun applyFilter() {
        classesToBeFiltered.forEach { klassItemFilter ->
            factory.createItem(klassItemFilter.name, klassItemFilter.packageString).isDisabled = true
        }
    }
}