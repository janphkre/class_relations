package de.janphkre.class_relations.library.domain.filter

import de.janphkre.class_relations.library.data.KlassItemFactory


class ExactPackageKlassFilter(
    private val packagesToBeFiltered: Set<String>,
    private val factory: KlassItemFactory
): KlassFilter {

    override fun applyFilter() {
        packagesToBeFiltered.forEach { packageString ->
            val items = factory.getItemsForPackage(packageString)
            items.forEach { item ->
                item.isDisabled = true
            }
        }
    }
}