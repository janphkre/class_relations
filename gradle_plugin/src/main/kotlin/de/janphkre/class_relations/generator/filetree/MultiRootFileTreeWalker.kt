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
import java.util.*

private typealias GroupedFiles = Collection<FileInRoot>

/**
 * Iterator for walking multiple file trees defined by [root] sharing the same folder structure in a grouped manner.
 *
 * The iterator is sorted in such a way, that only files are posted to it with a top-down depth first approach:
 * 1. iterate files in "rootA" & "rootB"
 * 2. onLeave for "rootA" & "rootB"
 * 3. iterate files in "rootA/a" & "rootB/a"
 * 4. onLeave for "rootA/a" & "rootB/a"
 * 5. iterate files in "rootB/b"
 * 6. onLeave for "rootB/b"
 * ...
 */
class MultiRootFileTreeWalker(roots: Collection<File>, private val onLeave: (OpenDirectory) -> Unit): Iterator<FileInRoot> {

    private val remainingDirectories = Stack<Iterator<GroupedFiles>>()
    private var openDirectory: OpenDirectory? = null
    private var openFileIterator: Iterator<FileInRoot>? = null

    init {
        val convertedRoots = roots.map { FileInRoot(it, it) }
        remainingDirectories.add(listOf(convertedRoots).iterator())
    }

    private fun computeNextDirectory(): Boolean {
        while (remainingDirectories.isNotEmpty() && !remainingDirectories.peek().hasNext()) {
            remainingDirectories.pop()
        }
        if (remainingDirectories.isEmpty()) {
            openDirectory = null
            return false
        }
        val nextDirectoryFiles = remainingDirectories.peek().next()
        val directory = nextDirectoryFiles.map { directory ->
            listFiles(
                directory = directory
            )
        }
        val subDirectories = directory.groupSubDirectories()
        openDirectory = OpenDirectory(subDirectories = subDirectories, directories = directory.map { it.directory })
        openFileIterator = GroupedDirectoriesFilesIterator(
            directory
        )
        remainingDirectories.push(subDirectories.values.iterator())
        if (!openFileIterator!!.hasNext()) {
            onLeave(openDirectory!!)
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
        if (openFileIterator!!.hasNext() ) {
            return true
        }
        onLeave(openDirectory!!)
        return computeNextDirectory()
    }

    override fun next(): FileInRoot {
        return openFileIterator!!.next()
    }

    private fun listFiles(directory: FileInRoot): Directory {
        val content = directory.file.listFiles() ?: emptyArray()
        val contentFiles = ArrayList<FileInRoot>(content.size)
        val contentDirectories = ArrayList<FileInRoot>(content.size)
        for (element in content) {
            if (element.isFile) {
                contentFiles.add(FileInRoot(file = element, root = directory.root))
            } else {
                contentDirectories.add(FileInRoot(file = element, root = directory.root))
            }
        }
        return Directory(contentFiles, directory, contentDirectories)
    }

    private fun List<Directory>.groupSubDirectories(): Map<String, GroupedFiles> {
        return this.flatMap { it.subDirectories }.groupBy { it.file.name }
    }

    private data class Directory(
        val files: List<FileInRoot>,
        val directory: FileInRoot,
        val subDirectories: List<FileInRoot>
    ) {
        val filesIterator
            get() = files.iterator()
    }

    private class GroupedDirectoriesFilesIterator(
        directories: List<Directory>
    ) : Iterator<FileInRoot> {

        private val outerIterator = directories.iterator()
        private var innerIterator: Iterator<FileInRoot>? = null

        private fun computeNextInner(): Boolean {
            while(outerIterator.hasNext()) {
                innerIterator = outerIterator.next().filesIterator
                if (innerIterator!!.hasNext()) {
                    return true
                }
            }
            return false
        }

        override fun hasNext(): Boolean {
            val innerIteratorNext = innerIterator?.hasNext()
            if (innerIteratorNext != true) {
                return computeNextInner()
            }
            return true
        }

        override fun next(): FileInRoot {
            return innerIterator!!.next()
        }
    }
}