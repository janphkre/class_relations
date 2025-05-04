package de.janphkre.class_relations.library.domain.filter

import de.janphkre.class_relations.library.data.KlassItemFactory
import de.janphkre.class_relations.library.model.KlassItemFilter


class WildcardKlassFilter(
    private val klassesToBeFiltered: Set<KlassItemFilter>,
    private val factory: KlassItemFactory
): KlassFilter {

    override fun applyFilter() {
        val storedPackages = factory.getAllPackages()
        klassesToBeFiltered.forEach { filter ->
            val packageRegex = Regex(filter.packageString)
            val nameRegex = Regex(filter.name)
            storedPackages.filter { packageRegex.matches(it) }.forEach { packageKey ->
                val items = factory.getItemsForPackage(packageKey)
                items.filter { item ->
                    nameRegex.matches(item.name)
                }.forEach { item ->
                    item.isDisabled = true
                }
            }
        }
    }
}