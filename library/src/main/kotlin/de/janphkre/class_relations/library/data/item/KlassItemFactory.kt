package de.janphkre.class_relations.library.data.item

import de.janphkre.class_relations.library.data.filter.KlassFilter
import de.janphkre.class_relations.library.model.KlassItem

interface KlassItemFactory {

    fun createItem(
        name: String,
        packageList: List<String>
    ): KlassItem

    fun createItem(
        name: String,
        packageString: String
    ): KlassItem

    fun clear()

    fun applyFilter(klassFilter: KlassFilter)

    companion object {

        private val klassItemFactory = KlassItemFactoryImpl()

        fun getInstance(): KlassItemFactory {
            return klassItemFactory
        }
    }
}
