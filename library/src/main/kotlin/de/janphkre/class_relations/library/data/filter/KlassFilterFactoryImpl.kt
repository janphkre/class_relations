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

class KlassFilterFactoryImpl : KlassFilterFactory {
    override fun createFilters(filtersData: List<String>): List<KlassFilter> {
        val globKlassFilterItems = HashSet<String>()
        val exactKlassFilterItems = HashSet<KlassFilterItem>()

        filtersData.forEach { filterString ->
            if (filterString.contains('*')) {
                globKlassFilterItems.add(filterString)
            } else {
                val splitIndex = filterString.indexOfLast { it == '.' }
                val filterItem = if (splitIndex < 0) {
                    KlassFilterItem(packageString = "", name = filterString)
                } else {
                    KlassFilterItem(
                        packageString = filterString.substring(0, splitIndex),
                        name = filterString.substring(splitIndex + 1)
                    )
                }
                exactKlassFilterItems.add(filterItem)
            }
        }
        val result = ArrayList<KlassFilter>(4)
        if(globKlassFilterItems.isNotEmpty()) {
            result.add(GlobKlassFilter(globKlassFilterItems))
        }
        if(exactKlassFilterItems.isNotEmpty()) {
            result.add(ExactKlassFilter(exactKlassFilterItems))
        }
        return result
    }
}