package de.janphkre.class_relations.library.data.filter

interface KlassFilterFactory {

    fun createFilters(filtersData: List<String>): List<KlassFilter>

    companion object {
        fun getInstance(): KlassFilterFactory {
            return KlassFilterFactoryImpl()
        }
    }
}