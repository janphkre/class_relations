package de.janphkre.class_relations.library.data.filter

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.data.item.KlassItemFactoryImpl
import de.janphkre.class_relations.library.model.KlassFilterItem
import org.junit.Test

class KlassFilterTest {

    @Test
    fun exactKlassFilter_matchesKlassItem() {
        val klassName = "ExampleKlassName"
        val klassPackage = "aaa.bbb.ccc"
        val klassItemFactory = KlassItemFactoryImpl()
        val klassItem = klassItemFactory.createItem(klassName, klassPackage)
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
        val klassItemFactory = KlassItemFactoryImpl()
        val klassItem = klassItemFactory.createItem(klassName, klassPackage)
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
        val klassItemFactory = KlassItemFactoryImpl()
        val klassItem = klassItemFactory.createItem(klassName, klassPackage)
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

    private fun globFilterMatches(klassName: String, klassPackage: String, globFilter: String) {
        val klassItemFactory = KlassItemFactoryImpl()
        val klassItem = klassItemFactory.createItem(klassName, klassPackage)
        val klassFilter = GlobKlassFilter(
            setOf(
                globFilter
            )
        )
        klassFilter.filterItem(klassItem)

        Truth.assertThat(klassItem.isDisabled).isTrue()
    }
}