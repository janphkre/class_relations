package de.janphkre.class_relations.library.data.item

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.data.filter.KlassFilterFactoryImpl
import org.junit.Test

class KlassItemFactoryTest {
    @Test
    fun createItemFromString_CreatesItemWithCorrectPackage() {
        val itemName = "ExampleItemName"
        val itemPackage = "aaa.bbb.ccc"

        val factory = KlassItemFactoryImpl()
        val item = factory.createItem(itemName, itemPackage)

        Truth.assertThat(item.name).isEqualTo(itemName)
        Truth.assertThat(item.filePackageString).isEqualTo(itemPackage)
        Truth.assertThat(item.filePackage).isEqualTo(itemPackage.split('.'))
    }

    @Test
    fun createItemFromString_CreatesItemWithCache() {
        val itemName = "ExampleItemName"
        val itemPackage = "aaa.bbb.ccc"

        val factory = KlassItemFactoryImpl()
        val item1 = factory.createItem(itemName, itemPackage)
        val item2 = factory.createItem(itemName, itemPackage)
        factory.clear()
        val item3 = factory.createItem(itemName, itemPackage)

        Truth.assertThat(item1).isSameInstanceAs(item2)
        Truth.assertThat(item1).isNotSameInstanceAs(item3)
    }

    @Test
    fun createItemFromString_CreatesWithEmptyRootPackage() {
        val itemName = "ExampleItemName"
        val itemPackage = ""

        val factory = KlassItemFactoryImpl()
        val item = factory.createItem(itemName, itemPackage)

        Truth.assertThat(item.name).isEqualTo(itemName)
        Truth.assertThat(item.filePackageString).isEqualTo(itemPackage)
        Truth.assertThat(item.filePackage).isEmpty()
    }

    @Test
    fun createItemFromList_CreatesItemWithCorrectPackage() {
        val itemName = "ExampleItemName"
        val itemPackage = listOf("aaa","bbb","ccc")

        val factory = KlassItemFactoryImpl()
        val item = factory.createItem(itemName, itemPackage)

        Truth.assertThat(item.name).isEqualTo(itemName)
        Truth.assertThat(item.filePackage).isEqualTo(itemPackage)
        Truth.assertThat(item.filePackageString).isEqualTo(itemPackage.joinToString("."))
    }

    @Test
    fun createItemFromList_CreatesWithEmptyRootPackage() {
        val itemName = "ExampleItemName"
        val itemPackage = emptyList<String>()

        val factory = KlassItemFactoryImpl()
        val item = factory.createItem(itemName, itemPackage)

        Truth.assertThat(item.name).isEqualTo(itemName)
        Truth.assertThat(item.filePackage).isEqualTo(itemPackage)
        Truth.assertThat(item.filePackageString).isEqualTo(itemPackage.joinToString("."))
    }
}