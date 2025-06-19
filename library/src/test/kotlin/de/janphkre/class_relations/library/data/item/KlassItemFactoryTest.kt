package de.janphkre.class_relations.library.data.item

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.data.filter.KlassFilterFactoryImpl
import de.janphkre.class_relations.library.model.AliasKlassItemImpl
import org.junit.Test

class KlassItemFactoryTest {

    @Test
    fun createItemFromList_CreatesItemWithCache() {
        val itemName = "ExampleItemName"
        val itemPackage = listOf("aaa","bbb","ccc")

        val factory = KlassItemFactoryImpl()
        val item1 = factory.createItem(name = itemName, packageList = itemPackage)
        val item2 = factory.createItem(name = itemName, packageList = itemPackage)
        factory.clear()
        val item3 = factory.createItem(name = itemName, packageList = itemPackage)

        Truth.assertThat(item1).isSameInstanceAs(item2)
        Truth.assertThat(item1).isNotSameInstanceAs(item3)
    }

    @Test
    fun createItemFromListWithAlias_CreatesItemWithCache() {
        val itemName = "ExampleItemName"
        val itemPackage = listOf("aaa","bbb","ccc")

        val factory = KlassItemFactoryImpl()
        val item1 = factory.createItem(name = itemName, codeIdentifier = "abcdefg", packageList = itemPackage)
        val item2 = factory.createItem(name = itemName, codeIdentifier = "hijklmn", packageList = itemPackage)
        val item3 = factory.createItem(name = itemName, packageList = itemPackage)

        Truth.assertThat(item1).isInstanceOf(AliasKlassItemImpl::class.java)
        Truth.assertThat(item2).isInstanceOf(AliasKlassItemImpl::class.java)
        Truth.assertThat(item1).isNotSameInstanceAs(item2)
        Truth.assertThat(item1).isNotSameInstanceAs(item3)
        Truth.assertThat(item2).isNotSameInstanceAs(item3)
        Truth.assertThat((item1 as AliasKlassItemImpl).delegate).isSameInstanceAs(item3)
        Truth.assertThat((item2 as AliasKlassItemImpl).delegate).isSameInstanceAs(item3)
    }

    @Test
    fun createItemFromList_CreatesItemWithCorrectPackage() {
        val itemName = "ExampleItemName"
        val itemPackage = listOf("aaa","bbb","ccc")

        val factory = KlassItemFactoryImpl()
        val item = factory.createItem(name = itemName, packageList = itemPackage)

        Truth.assertThat(item.name).isEqualTo(itemName)
        Truth.assertThat(item.filePackage).isEqualTo(itemPackage)
        Truth.assertThat(item.filePackageString).isEqualTo(itemPackage.joinToString("."))
    }

    @Test
    fun createItemFromList_CreatesWithEmptyRootPackage() {
        val itemName = "ExampleItemName"
        val itemPackage = emptyList<String>()

        val factory = KlassItemFactoryImpl()
        val item = factory.createItem(name = itemName, packageList = itemPackage)

        Truth.assertThat(item.name).isEqualTo(itemName)
        Truth.assertThat(item.filePackage).isEqualTo(itemPackage)
        Truth.assertThat(item.filePackageString).isEqualTo(itemPackage.joinToString("."))
    }
}