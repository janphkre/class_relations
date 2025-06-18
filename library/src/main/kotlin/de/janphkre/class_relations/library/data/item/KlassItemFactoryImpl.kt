package de.janphkre.class_relations.library.data.item

import de.janphkre.class_relations.library.data.filter.KlassFilter
import de.janphkre.class_relations.library.model.KlassItem
import de.janphkre.class_relations.library.model.KlassItemImpl
import de.janphkre.class_relations.library.model.UnclearKlassItemImpl

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

    override fun createItem(name: String): KlassItem {
        val klassItem = UnclearKlassItemImpl(
            name = name,
            filePackage = emptyList(),
            filePackageString = "",
            isDisabled = false
        )
        filterItem(klassItem)
        return klassItem
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
        val newResultItem = KlassItemImpl(name, filePackageList, filePackageString)
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

    override fun applyFilters(klassFilters: List<KlassFilter>) {
        filters.addAll(klassFilters)
    }

    override fun clear() {
        itemsCache.clear()
        filters.forEach { it.resetCache() }
    }
}
