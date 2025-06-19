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

import de.janphkre.class_relations.library.model.KlassItem
import de.janphkre.class_relations.library.model.KlassWithRelations
import de.janphkre.class_relations.library.model.UnclearKlassItem

class KlassDisabledFilteringImpl: KlassDisabledFiltering {
    override fun filter(klasses: List<KlassWithRelations>): List<KlassWithRelations> {
        return klasses.mapNotNull { klass ->
            if (klass.item.isDisabled) {
                return@mapNotNull null
            }
            return@mapNotNull klass.copy(
                fileImports = klass.fileImports.filterAndRectifyBy(klasses),
                parameters = klass.parameters.filterAndRectifyBy(klasses),
                methodParameters = klass.methodParameters.filterAndRectifyBy(klasses),
                inheritances = klass.inheritances.filterAndRectifyBy(klasses),
            )
        }
    }

    private fun List<KlassItem>.filterAndRectifyBy(currentKlasses: List<KlassWithRelations>): List<KlassItem> {
        return map { import ->
            if (import !is UnclearKlassItem) {
                return@map import
            }
            val localItem = currentKlasses.firstOrNull { it.item.name == import.name }
            return@map localItem?.item ?: import
        }.filterNot { it.isDisabled }
    }
}