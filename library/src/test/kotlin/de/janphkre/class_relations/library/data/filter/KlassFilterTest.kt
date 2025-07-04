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
import de.janphkre.class_relations.library.data.item.KlassItemFactoryImpl
import de.janphkre.class_relations.library.model.KlassFilterItem
import de.janphkre.class_relations.library.model.KlassItemImpl
import org.junit.Test

class KlassFilterTest {

    @Test
    fun exactKlassFilter_matchesKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val klassItem = KlassItemImpl(
            name = klassName,
            filePackage = klassPackage.split("."),
            filePackageString = klassPackage
        )
        val klassFilter = ExactKlassFilter(
            listOf(
                KlassFilterItem(klassName, klassPackage)
            )
        )
        klassFilter.filterItem(klassItem)

        Truth.assertThat(klassItem.isDisabled).isTrue()
    }

    @Test
    fun exactKlassFilter_doesNotMatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val filterPackage = "aaa.bbb.ddd"
        val klassItem = KlassItemImpl(
            name = klassName,
            filePackage = klassPackage.split("."),
            filePackageString = klassPackage
        )
        val klassFilter = ExactKlassFilter(
            listOf(
                KlassFilterItem(klassName, filterPackage)
            )
        )
        klassFilter.filterItem(klassItem)

        Truth.assertThat(klassItem.isDisabled).isFalse()
    }

    @Test
    fun globPackageFilter_matchesExactKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "aaa.bbb.ccc.ExampleKlassName"
        globFilterMatches(klassName = klassName, klassPackage = klassPackage, globFilter = globFilter)
    }

    @Test
    fun globPackageFilter_matchesWildcardKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "aaa.bbb.*.ExampleKlassName"
        globFilterMatches(klassName = klassName, klassPackage = klassPackage, globFilter = globFilter)
    }

    @Test
    fun globPackageFilter_doesNotMatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val filterPackage = "bbb.*.ExampleKlassName"
        val klassItem = KlassItemImpl(
            name = klassName,
            filePackage = klassPackage.split("."),
            filePackageString = klassPackage
        )
        val klassFilter = GlobKlassFilter(
            setOf(
                filterPackage
            )
        )
        klassFilter.filterItem(klassItem)

        Truth.assertThat(klassItem.isDisabled).isFalse()
    }

    @Test
    fun globFilterExactMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "aaa.bbb.ccc.ExampleKlassName"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterStarMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "*.bbb.*.ExampleKlassName"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterPathWildcardMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "**.ExampleKlassName"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterMiddleStarMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "aaa.**.Example*Name"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterEndPathWildcardMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "aaa.**"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterMiddleWildcardMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "a*a.bbb.ccc.ExampleKlassName"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterFullWildcardMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "**"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterSingleWildcardMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = ""
        val globFilter = "*"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterMultiplePathWildcardMatches_MatchKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val globFilter = "**.**.**"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    @Test
    fun globFilterMultiplePathWildcardMatches_MatchSinglePackageKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa"
        val globFilter = "**.**.**"
        globFilterMatches(klassName, klassPackage, globFilter)
    }

    private fun globFilterMatches(klassName: String, klassPackage: String, globFilter: String) {
        val filePackageList = if(klassPackage.isBlank()) {
            emptyList()
        } else {
            klassPackage.split(".")
        }
        val klassItem = KlassItemImpl(
            name = klassName,
            filePackage = filePackageList,
            filePackageString = klassPackage
        )
        val klassFilter = GlobKlassFilter(
            setOf(
                globFilter
            )
        )
        klassFilter.filterItem(klassItem)

        Truth.assertThat(klassItem.isDisabled).isTrue()
    }
}