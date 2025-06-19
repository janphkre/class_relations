/**
 *    Copyright 2025 Jan Phillip Kretzschmar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.janphkre.class_relations.library.data.item

import de.janphkre.class_relations.library.data.filter.KlassFilter
import de.janphkre.class_relations.library.model.AliasKlassItemImpl
import de.janphkre.class_relations.library.model.KlassItem
import de.janphkre.class_relations.library.model.KlassItemImpl
import de.janphkre.class_relations.library.model.UnclearKlassItemImpl

class KlassItemFactoryImpl: KlassItemFactory {

    private val itemsCache: MutableMap<String, MutableMap<String, KlassItem>> = HashMap()
    private val filters: MutableCollection<KlassFilter> = ArrayList()

    override fun createItem(
        name: String,
        codeIdentifier: String?,
        packageList: List<String>
    ): KlassItem {
        val item = createItem(
            name,
            packageList,
            packageList.joinToString(".")
        )
        if (codeIdentifier == null) {
            return item
        }
        return AliasKlassItemImpl(
            item,
            codeIdentifier
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
        val newResultItem = KlassItemImpl(
            name = name,
            filePackage = filePackageList,
            filePackageString = filePackageString
        )
        itemsInPackage[name] = newResultItem
        filterItem(newResultItem)
        return newResultItem
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
