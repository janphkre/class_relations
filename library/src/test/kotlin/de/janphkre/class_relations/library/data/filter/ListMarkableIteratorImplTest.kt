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