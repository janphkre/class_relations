package de.janphkre.class_relations.generator

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
class SortedFileTreeWalker(root: File, private val onLeave: (File) -> Unit): Iterator<File> {

    private val remainingDirectories = Stack<Iterator<File>>()
    private var openDirectory: Directory? = null

    init {
        remainingDirectories.add(listOf(root).iterator())
    }

    private fun computeNext() {
        while (remainingDirectories.isNotEmpty() && !remainingDirectories.peek().hasNext()) {
            remainingDirectories.pop()
        }
        if (remainingDirectories.isEmpty()) {
            openDirectory = null
            return
        }
        val directory = listFiles(remainingDirectories.peek().next())
        openDirectory = directory
        remainingDirectories.push(directory.subDirectories)
    }

    override fun hasNext(): Boolean {
        if (remainingDirectories.isEmpty()) {
            return false
        }
        if (openDirectory!!.files.hasNext() ) {
            return true
        }
        onLeave(openDirectory!!.directory)
        computeNext()
        return openDirectory != null
    }

    override fun next(): File {
        return openDirectory!!.files.next()
    }

    private fun listFiles(directory: File): Directory {
        val content = directory.listFiles() ?: emptyArray()
        val contentFiles = ArrayList<File>( content.size + 1)
        val contentDirectories = ArrayList<File>(content.size)
        for (element in content) {
            if (element.isFile) {
                contentFiles.add(element)
            } else {
                contentDirectories.add(element)
            }
        }
        contentFiles.add(directory)
        return Directory(contentFiles.iterator(), directory, contentDirectories.iterator())
    }

    private data class Directory(
        val files: Iterator<File>,
        val directory: File,
        val subDirectories: Iterator<File>
    )
}