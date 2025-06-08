package de.janphkre.class_relations.library.data.filter

import de.janphkre.class_relations.library.model.KlassItem

/**
 * Filters the provided item by a defined condition.
 */
interface KlassFilter {
    fun filterItem(item: KlassItem)
}