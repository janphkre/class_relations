package de.janphkre.class_relations.library.data.filter

import de.janphkre.class_relations.library.model.KlassWithRelations

interface KlassDisabledFiltering {
    fun filter(klasses: List<KlassWithRelations>): List<KlassWithRelations>

    companion object {
        fun getInstance(): KlassDisabledFiltering {
            return KlassDisabledFilteringImpl()
        }
    }
}