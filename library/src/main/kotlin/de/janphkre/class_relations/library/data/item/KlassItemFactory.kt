package de.janphkre.class_relations.library.data.item

import de.janphkre.class_relations.library.data.filter.KlassFilter
import de.janphkre.class_relations.library.model.KlassItem

interface KlassItemFactory {

    fun createItem(
        name: String,
        codeIdentifier: String? = null,
        packageList: List<String>
    ): KlassItem

    /**
     * Creates a class item with no predetermined package.
     */
    fun createItem(
        name: String
    ): KlassItem

    fun clear()

    fun applyFilters(klassFilters: List<KlassFilter>)

    companion object {

        private val klassItemFactory = KlassItemFactoryImpl()

        fun getInstance(): KlassItemFactory {
            return klassItemFactory
        }
    }
}
