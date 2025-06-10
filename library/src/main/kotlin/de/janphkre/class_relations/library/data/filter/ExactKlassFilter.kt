package de.janphkre.class_relations.library.data.filter

import de.janphkre.class_relations.library.model.KlassFilterItem
import de.janphkre.class_relations.library.model.KlassItem
import org.jetbrains.annotations.TestOnly

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