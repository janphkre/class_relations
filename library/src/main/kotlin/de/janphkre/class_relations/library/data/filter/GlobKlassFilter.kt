package de.janphkre.class_relations.library.data.filter

import de.janphkre.class_relations.library.model.KlassItem


class GlobKlassFilter(
    internal val klassesToBeFiltered: Collection<String>
): KlassFilter {

    private val globFilters = klassesToBeFiltered.map { filterItem ->
        GlobFilter(filterItem.split('.').map { element ->
            when{
                element == "**" -> GlobFilterElement.Wildpath
                element == "*" -> GlobFilterElement.Wildcard
                element.contains('*') -> GlobFilterElement.Match(element.split('*'))
                else -> GlobFilterElement.Exact(element)
            }
        })
    }

    private sealed interface GlobFilterElement {
        data class Exact(val exact: String): GlobFilterElement
        data class Match(val matchElements: List<String>): GlobFilterElement
        data object Wildpath: GlobFilterElement
        data object Wildcard: GlobFilterElement
    }

    private class GlobFilter(
        val elements: List<GlobFilterElement>
    )

    private class GlobFilterSearch(
        globFilter: GlobFilter,
        target: List<String>
    ){
        private var hasMark: Boolean = false
        private val targetIterator: MarkableIterator<String> = ListMarkableIteratorImpl(target)
        private val globIterator: MarkableIterator<GlobFilterElement> = ListMarkableIteratorImpl(globFilter.elements)

        fun checkGlob(): Boolean {
            while(globIterator.hasNext()) {
                val globElement = globIterator.next()
                when(globElement) {
                    is GlobFilterElement.Exact -> {
                        if (!targetIterator.hasNext()) {
                            return false
                        }
                        val targetElement = targetIterator.next()
                        if (!checkGlobElement(targetElement, globElement) && !restoreMark()) {
                            return false
                        }
                        continue
                    }
                    is GlobFilterElement.Match -> {
                        if (!targetIterator.hasNext()) {
                            return false
                        }
                        val targetElement = targetIterator.next()
                        if (!checkGlobElement(targetElement, globElement) && !restoreMark()) {
                            return false
                        }
                        continue
                    }
                    GlobFilterElement.Wildpath -> {
                        setMark()
                        continue
                    }
                    GlobFilterElement.Wildcard -> {
                        if (!targetIterator.hasNext()) {
                            return false
                        }
                        targetIterator.next()
                        continue
                    }
                }
            }
            if (globIterator.previous() == GlobFilterElement.Wildpath) {
                return true
            }
            return !targetIterator.hasNext()
        }

        private fun setMark() {
            targetIterator.mark()
            globIterator.mark()
            hasMark = true
        }


        private fun restoreMark(): Boolean {
            if (!hasMark) {
                return false
            }
            globIterator.resetToMark()
            targetIterator.resetToMark()
            targetIterator.next()
            targetIterator.mark()
            return true
        }

        private fun checkGlobElement(targetElement: String, globElement: GlobFilterElement.Exact): Boolean {
            return targetElement == globElement.exact

        }

        private fun checkGlobElement(targetElement: String, globElement: GlobFilterElement.Match): Boolean {
            var index = 0
            val startMatch = globElement.matchElements.first()
            if (startMatch.isNotEmpty()) {
                if (!targetElement.startsWith(startMatch)) {
                    return false
                }
                index = startMatch.length
            }
            for (matchIndex in 1 until globElement.matchElements.size - 1) {
                if (index >= targetElement.length) {
                    return false
                }
                val matchElement = globElement.matchElements[matchIndex]
                if (matchElement.isEmpty()) {
                    //Double star can be ignored / treat as single star
                    continue
                }
                val targetIndex = targetElement.contains(matchElement, index)
                if (targetIndex < 0) {
                    return false
                }
                index = targetIndex + matchElement.length
            }
            val endMatch = globElement.matchElements.last()
            if (endMatch.isEmpty()) {
                return true
            }
            if (index + endMatch.length >= targetElement.length) {
                return false
            }
            return targetElement.endsWith(endMatch)
        }

        private fun String.contains(content: String, startIndex: Int): Int {
            for (i in startIndex until this.length - content.length) {
                if (this.startsWith(content, i)) {
                    return i
                }
            }
            return  -1
        }
    }

    override fun filterItem(item: KlassItem) {
        globFilters.forEach { filter ->
            if (GlobFilterSearch(filter, item.filePackage.plus(item.name)).checkGlob()) { //TODO: FIND A BETTER WAY FOR THIS
                item.isDisabled = true
                return@forEach
            }
        }
    }
}