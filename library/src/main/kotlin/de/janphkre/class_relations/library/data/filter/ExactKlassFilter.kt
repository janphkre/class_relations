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
package de.janphkre.class_relations.library.data.filter

import de.janphkre.class_relations.library.model.KlassFilterItem
import de.janphkre.class_relations.library.model.KlassItem

class ExactKlassFilter(
    internal val classesToBeFiltered: Collection<KlassFilterItem>
): KlassFilter {

    private var activeFilters = classesToBeFiltered.toMutableSet()

    override fun filterItem(item: KlassItem) {
        val isFiltered = activeFilters.removeFirst { filter ->
            item.name == filter.name && item.filePackageString == filter.packageString
        }
        if (isFiltered) {
            item.isDisabled = true
        }
    }

    private fun <T: Any> MutableCollection<T>.removeFirst(lambda: (T) -> Boolean): Boolean {
        val iterator = iterator()
        while(iterator.hasNext()) {
            if (lambda.invoke(iterator.next())) {
                iterator.remove()
                return true
            }
        }
        return false
    }

    override fun resetCache() {
        activeFilters = classesToBeFiltered.toMutableSet()
    }
}