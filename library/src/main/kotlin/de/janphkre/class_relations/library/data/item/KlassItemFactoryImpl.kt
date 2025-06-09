package de.janphkre.class_relations.library.data.item

import de.janphkre.class_relations.library.data.filter.KlassFilter
import de.janphkre.class_relations.library.model.KlassItem

class KlassItemFactoryImpl: KlassItemFactory {

    private val itemsCache: MutableMap<String, MutableMap<String, KlassItem>> = HashMap()
    private val filters: MutableCollection<KlassFilter> = ArrayList()

    override fun createItem(
        name: String,
        packageList: List<String>
    ): KlassItem {
        return createItem(
            name,
            packageList,
            packageList.joinToString(".")
        )
    }

    private fun createItem(
        name: String,
        filePackageList: List<String>,
        filePackageString: String
    ): KlassItem {
        var itemsInPackage = itemsCache[filePackageString]
        if (itemsInPackage != null) {
            val resultItem = itemsInPackage[name]
            if (resultItem != null) {
                return resultItem
            }
        } else {
            itemsInPackage = HashMap()
            itemsCache[filePackageString] = itemsInPackage
        }
        val newResultItem = KlassItem(name, filePackageList, filePackageString)
        itemsInPackage[name] = newResultItem
        filterItem(newResultItem)
        return newResultItem
    }

    override fun createItem(
        name: String,
        packageString: String
    ): KlassItem {
        return if (packageString.isEmpty()) {
            createItem(
                name,
                emptyList(),
                ""
            )
        } else {
            createItem(
                name,
                packageString.split("."),
                packageString
            )
        }
    }

    private fun filterItem(item: KlassItem) {
        for (filter in filters) {
            filter.filterItem(item)
            if (item.isDisabled) {
                return
            }
        }
    }

    override fun applyFilter(klassFilter: KlassFilter) {
        filters.add(klassFilter)
    }

    override fun clear() {
        itemsCache.clear()
    }
}
