package de.janphkre.class_relations.generator.filetree

import ch.tutteli.atrium.api.fluent.en_GB.toContainExactlyElementsOf
import ch.tutteli.atrium.api.verbs.expect
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.Test

class MultiRootFileTreeWalkerTest {

    @Test
    fun multiRootFileTreeWalkerIteratesFilesAsExpected() {
        val input1 = mockDir(
            "root1",
            mockFile("a1"),
            mockDir(
                "b",
                mockFile("c1"),
                mockFile("d1")
            ),
            mockFile("e1"),
            mockDir(
                "f",
                mockFile("g1"),
                mockDir(
                    "h",
                    mockFile("i1")
                ),
                mockFile("j1")
            ),
            mockDir("k",
                mockDir("l1")
            ),
            mockFile("m1"),
            mockDir(
                "n1",
                mockFile("o1"),
                mockFile("p1")
            ),
        )
        val input2 = mockDir(
            "root2",
            mockFile("a2"),
            mockDir(
                "b",
                mockFile("c2"),
                mockFile("d2")
            ),
            mockFile("e2"),
            mockDir("f"),
            mockDir("k",
                mockDir("l2")
            ),
            mockFile("m2"),
            mockDir(
                "n2",
                mockFile("o2"),
                mockFile("p2")
            ),
        )
        val expectedResult = listOf(
            WalkerItem("a1", "root1", false),
            WalkerItem("e1", "root1", false),
            WalkerItem("m1", "root1", false),
            WalkerItem("a2", "root2", false),
            WalkerItem("e2", "root2", false),
            WalkerItem("m2", "root2", false),
            WalkerItem("root1;root2", "root1;root2", true),
            WalkerItem("c1", "root1", false),
            WalkerItem("d1", "root1", false),
            WalkerItem("c2", "root2", false),
            WalkerItem("d2", "root2", false),
            WalkerItem("b;b", "root1;root2",  true),
            WalkerItem("g1",  "root1",false),
            WalkerItem("j1",  "root1",false),
            WalkerItem("f;f", "root1;root2",  true),
            WalkerItem("i1",  "root1",false),
            WalkerItem("h", "root1",  true),
            WalkerItem("k;k", "root1;root2",  true),
            WalkerItem("l1", "root1", true),
            WalkerItem("l2", "root2", true),
            WalkerItem("o1",  "root1",false),
            WalkerItem("p1",  "root1",false),
            WalkerItem("n1", "root1", true),
            WalkerItem("o2",  "root2",false),
            WalkerItem("p2",  "root2",false),
            WalkerItem("n2", "root2", true),
        )

        val result = ArrayList<WalkerItem>()
        val walker = MultiRootFileTreeWalker(
            listOf(
                input1,
                input2
            )
        ) { directories ->
            val directoryNames = directories.directories.map { it.file.name }.joinToString(";")
            val rootNames = directories.directories.map { it.root.name }.joinToString(";")
            result.add(WalkerItem(
                directoryNames,
                rootNames,
                true
            ))
        }
        walker.forEach { result.add(WalkerItem(it.file.name, it.root.name, false)) }
        expect(result).toContainExactlyElementsOf(expectedResult)
    }

    private data class WalkerItem(
        val fileName: String,
        val rootName: String,
        val fromOnLeave: Boolean
    )

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