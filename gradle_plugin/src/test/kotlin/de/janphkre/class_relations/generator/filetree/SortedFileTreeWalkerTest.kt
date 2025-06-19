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