package de.janphkre.class_relations.library.data.filter

class ListMarkableIteratorImpl<T: Any>(private val list: List<T>): MarkableIterator<T> {

    private var iterator = list.listIterator()
    private var markIndex = 0

    override fun mark() {
        markIndex = iterator.nextIndex()
    }

    override fun resetToMark() {
        iterator = list.listIterator(markIndex)
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun hasPrevious(): Boolean {
        return iterator.hasPrevious()
    }

    override fun next(): T {
        return iterator.next()
    }

    override fun nextIndex(): Int {
        return iterator.nextIndex()
    }

    override fun previous(): T {
        return iterator.previous()
    }

    override fun previousIndex(): Int {
        return iterator.previousIndex()
    }
}