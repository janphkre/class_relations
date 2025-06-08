package de.janphkre.class_relations.library.data.filter

import de.janphkre.class_relations.library.model.KlassFilterItem
import de.janphkre.class_relations.library.model.KlassItem
import org.jetbrains.annotations.TestOnly

class ExactKlassFilter(
    internal val classesToBeFiltered: Collection<KlassFilterItem>
): KlassFilter {

    override fun filterItem(item: KlassItem) {
        classesToBeFiltered.forEach { klassItemFilter ->
            if (item.name == klassItemFilter.name && item.filePackageString == klassItemFilter.packageString) {
                item.isDisabled = true
                return
            }
        }
    }
}