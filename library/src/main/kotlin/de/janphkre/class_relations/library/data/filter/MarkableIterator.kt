package de.janphkre.class_relations.library.data.filter

interface MarkableIterator<T: Any>: ListIterator<T> {

    fun mark()
    fun resetToMark()
}