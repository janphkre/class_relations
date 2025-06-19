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
package de.janphkre.class_relations.generator.filetree

import java.io.File
import java.util.Stack

/**
 * Iterator for walking a file tree defined by [root].
 * The iterator is sorted in such a way, that only files are posted to it with a top-down depth first approach:
 * 1. iterate files in "root"
 * 2. onLeave for "root"
 * 3. iterate files in "root/a"
 * 4. onLeave for "root/a"
 * 5. iterate files in "root/b"
 * 6. onLeave for "root/b"
 * ...
 */
class SortedFileTreeWalker(root: File, private val onLeave: (Directory) -> Unit): Iterator<File> {

    private val remainingDirectories = Stack<Iterator<File>>()
    private var openDirectory: Directory? = null

    init {
        remainingDirectories.add(listOf(root).iterator())
    }

    private fun computeNextDirectory(): Boolean {
        while (remainingDirectories.isNotEmpty() && !remainingDirectories.peek().hasNext()) {
            remainingDirectories.pop()
        }
        if (remainingDirectories.isEmpty()) {
            openDirectory = null
            return false
        }
        val directory = listFiles(remainingDirectories.peek().next())
        openDirectory = directory
        remainingDirectories.push(directory.subDirectoriesIterator)
        if (!directory.filesIterator.hasNext()) {
            onLeave(directory)
            return computeNextDirectory()
        }
        return true
    }

    override fun hasNext(): Boolean {
        if (remainingDirectories.isEmpty()) {
            return false
        }
        if(openDirectory == null) {
            return computeNextDirectory()
        }
        if (openDirectory!!.filesIterator.hasNext() ) {
            return true
        }
        onLeave(openDirectory!!)
        return computeNextDirectory()
    }

    override fun next(): File {
        return openDirectory!!.filesIterator.next()
    }

    private fun listFiles(directory: File): Directory {
        val content = directory.listFiles() ?: emptyArray()
        val contentFiles = ArrayList<File>(content.size)
        val contentDirectories = ArrayList<File>(content.size)
        for (element in content) {
            if (element.isFile) {
                contentFiles.add(element)
            } else {
                contentDirectories.add(element)
            }
        }
        return Directory(contentFiles, directory, contentDirectories)
    }

    data class Directory(
        val files: List<File>,
        val directory: File,
        val subDirectories: List<File>
    ) {
        internal val filesIterator = files.iterator()
        internal val subDirectoriesIterator = subDirectories.iterator()
    }
}