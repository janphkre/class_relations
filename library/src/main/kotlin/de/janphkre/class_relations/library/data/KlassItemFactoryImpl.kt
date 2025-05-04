package de.janphkre.class_relations.library.data

import de.janphkre.class_relations.library.model.KlassItem

class KlassItemFactoryImpl: KlassItemFactory {

    private val itemsCache: MutableMap<String, MutableMap<String, KlassItem>> = HashMap()

    override fun createItem(
        name: String,
        packageList: List<String>
    ): KlassItem {
        val filePackageString = packageList.joinToString(".")
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
        val newResultItem = KlassItem(name, packageList, filePackageString)
        itemsInPackage[name] = newResultItem
        return newResultItem
    }

    override fun createItem(
        name: String,
        packageString: String
    ): KlassItem {
        var itemsInPackage = itemsCache[packageString]
        if (itemsInPackage != null) {
            val resultItem = itemsInPackage[name]
            if (resultItem != null) {
                return resultItem
            }
        } else {
            itemsInPackage = HashMap()
            itemsCache[packageString] = itemsInPackage
        }
        val newResultItem = KlassItem(name, packageString.split("."), packageString)
        itemsInPackage[name] = newResultItem
        return newResultItem
    }

    override fun getItemsForPackage(packageString: String): Collection<KlassItem> {
        return itemsCache[packageString]?.values ?: emptyList()
    }

    override fun getAllPackages(): Collection<String> {
        return itemsCache.keys
    }
}
