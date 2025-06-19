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

import com.google.common.truth.Truth
import org.junit.Test

class ListMarkableIteratorImplTest {

    @Test
    fun listProvided_CanIterateAllNext() {
        val list = listOf("A", "B", "C", "D")
        val testIterator = ListMarkableIteratorImpl(list)
        val resultIterator = list.listIterator()

        while(resultIterator.hasNext()) {
            Truth.assertThat(testIterator.hasNext()).isTrue()
            Truth.assertThat(testIterator.next()).isEqualTo(resultIterator.next())
            Truth.assertThat(testIterator.nextIndex()).isEqualTo(resultIterator.nextIndex())
        }
        Truth.assertThat(testIterator.hasNext()).isFalse()
        Truth.assertThat(testIterator.nextIndex()).isEqualTo(4)
    }

    @Test
    fun listProvided_CanIterateAllPrevious() {
        val list = listOf("A", "B", "C", "D")
        val testIterator = ListMarkableIteratorImpl(list)
        val resultIterator = list.listIterator()

        while(resultIterator.hasPrevious()) {
            Truth.assertThat(testIterator.hasPrevious()).isTrue()
            Truth.assertThat(testIterator.previous()).isEqualTo(resultIterator.previous())
            Truth.assertThat(testIterator.previousIndex()).isEqualTo(resultIterator.previousIndex())
        }
        Truth.assertThat(testIterator.hasPrevious()).isFalse()
        Truth.assertThat(testIterator.nextIndex()).isEqualTo(0)
    }

    @Test
    fun listProvided_CanResetToMark() {
        val list = listOf("A", "B", "C", "D")
        val testIterator = ListMarkableIteratorImpl(list)

        testIterator.next()
        Truth.assertThat(testIterator.next()).isEqualTo(list[1])
        testIterator.mark()
        testIterator.next()
        Truth.assertThat(testIterator.next()).isEqualTo(list[3])
        testIterator.resetToMark()
        Truth.assertThat(testIterator.next()).isEqualTo(list[2])
    }
}