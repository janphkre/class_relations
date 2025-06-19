/**
 *    Copyright 2025 Jan Phillip Kretzschmar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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