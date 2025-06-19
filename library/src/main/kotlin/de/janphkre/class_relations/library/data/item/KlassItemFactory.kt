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
