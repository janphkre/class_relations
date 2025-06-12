package de.janphkre.class_relations.generator.filetree

import ch.tutteli.atrium.api.fluent.en_GB.toContainExactlyElementsOf
import ch.tutteli.atrium.api.verbs.expect
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.Test

internal class SortedFileTreeWalkerTest {

    private data class WalkerItem(
        val fileName: String,
        val fromOnLeave: Boolean
    )

    @Test
    fun sortedFileTreeWalkerIteratesFilesAsExpected() {
        val input = mockDir(
            "root",
            mockFile("a"),
            mockDir(
                "b",
                mockFile("c"),
                mockFile("d")
            ),
            mockFile("e"),
            mockDir(
                "f",
                mockFile("g"),
                mockDir(
                    "h",
                    mockFile("i")
                ),
                mockFile("j")
            ),
            mockDir("k",
                mockDir("l")
            ),
            mockFile("m")
        )
        val expectedResult = listOf(
            WalkerItem("a", false),
            WalkerItem("e", false),
            WalkerItem("m", false),
            WalkerItem("root", true),
            WalkerItem("c", false),
            WalkerItem("d", false),
            WalkerItem("b", true),
            WalkerItem("g", false),
            WalkerItem("j", false),
            WalkerItem("f", true),
            WalkerItem("i", false),
            WalkerItem("h", true),
            WalkerItem("k", true),
            WalkerItem("l", true),
        )

        val result = ArrayList<WalkerItem>()
        val walker = SortedFileTreeWalker(input) { result.add(WalkerItem(it.directory.name, true))}
        walker.forEach { result.add(WalkerItem(it.name, false)) }
        expect(result).toContainExactlyElementsOf(expectedResult)
    }

    private fun mockDir(name: String, vararg entries: File): File {
        return mockk<File> {
            every { isFile } returns false
            every { listFiles() } returns entries
            every { this@mockk.name } returns name
        }
    }

    private fun mockFile(name: String): File {
        return mockk<File> {
            every { isFile } returns true
            every { this@mockk.name } returns name
        }
    }
}