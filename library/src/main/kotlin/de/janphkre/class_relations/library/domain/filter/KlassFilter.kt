package de.janphkre.class_relations.library.domain.filter

import de.janphkre.class_relations.library.model.KlassWithRelations

/**
 * Filters the provided list of klasses by a defined condition.
 *
 * TODO: FILTERS COULD BE MORE EFFICIENT IF KLASSITEM WAS he same instance for the same klass!...
 * -> FILTER THEN ONLY WITH A SOFT DELETE..
 */
interface KlassFilter {
    fun applyFilter()
}