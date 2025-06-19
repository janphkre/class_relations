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

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.model.KlassFilterItem
import org.junit.Test

class KlassFilterFactoryTest {

    @Test
    fun createFilterWithEmptyList_CreatesEmptyFilterList() {
        val filtersData = emptyList<String>()
        val factory = KlassFilterFactoryImpl()

        val resultFilters = factory.createFilters(filtersData)

        Truth.assertThat(resultFilters).isEmpty()
    }

    @Test
    fun createFilter_CreatesExactMatchFilter() {
        val filtersData = listOf(
            "aaa.bbb.ExactExample1",
            "eee.ExactExample2",
            "aaa.ddd.ExactExample3",
            "aaa.bbb.cccExactExample4",
            "cccExactExample5"
        )
        val factory = KlassFilterFactoryImpl()

        val resultFilters = factory.createFilters(filtersData)

        Truth.assertThat(resultFilters).hasSize(1)
        val filterElement = resultFilters.first() as ExactKlassFilter

        Truth.assertThat(filterElement.classesToBeFiltered).containsExactlyElementsIn(filtersData.toKlassItem())
    }

    @Test
    fun createFilter_CreatesGlobMatchFilter() {
        val filtersData = listOf(
            "aaa.bbb.*",
            "ccc.Wildcard*Example2",
            "ddd.*.WildcardExample3",
            "*",
            "**",
            "**.WildcardExample6",
            "**.**.**",
            "*.WildcardExample8",
            "eee.Wildcard**Example9",
            "f*f.WildcardExample10",
            "**.WildcardExample11",
            "ggg.**"
        )
        val factory = KlassFilterFactoryImpl()

        val resultFilters = factory.createFilters(filtersData)

        Truth.assertThat(resultFilters).hasSize(1)
        val filterElement = resultFilters.first() as GlobKlassFilter

        Truth.assertThat(filterElement.klassesToBeFiltered).containsExactlyElementsIn(filtersData)
    }

    private fun List<String>.toKlassItem(): List<KlassFilterItem> {
        return map {
            val name = it.substringAfterLast('.')
            val packageString = if (it.length == name.length) {
                ""
            } else {
                it.substring(0, it.length - name.length - 1)
            }
            KlassFilterItem(
                name = name,
                packageString = packageString
            )
        }
    }
}