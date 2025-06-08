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