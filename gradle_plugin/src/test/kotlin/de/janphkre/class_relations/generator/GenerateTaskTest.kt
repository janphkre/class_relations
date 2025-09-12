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
package de.janphkre.class_relations.generator

import com.google.common.truth.Truth
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GenerateTaskTest {

    @get:Rule
    val testDirectory = TemporaryFolder()

    @Test
    fun actionOnBase_runsThroughFiles() {
        val destinationFolder = testDirectory.newFolder("destination")
        val sourceFolder = testDirectory.newFolder("sources")
        fillSourceFolder(usecase = "basic_example", target = sourceFolder)

        val buildFile = testDirectory.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'com.github.janphkre.class_relations'
            }
            
            pumlGenerate {
                projectPackagePrefix = "basic_example"
                selfColor = "#00FF00"
                spaceCount = 4
                generatedFileName = "example_relations.puml"
                destination = new File("${destinationFolder.absolutePath.replace('\\','/')}")
                sources = [new File("${sourceFolder.absolutePath.replace('\\','/')}")]
                filters = [
                    "io.other.UsageClassD",
                    "com.example.b.ParameterClassC"
                ]
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testDirectory.root)
            .withPluginClasspath()
            .withArguments("generateClassRelationsPuml")
            .forwardOutput()
            .build()

        destinationFolder.printDestinationStructure()

        Truth.assertThat(readFile(File(destinationFolder, "basic_example/example_relations.puml")))
            .isEqualTo(readFile("src/test/resources/basic_example__expected_generate_output.puml"))
    }


    @Test
    fun actionOnDepth_createsEmptyDiagramsForStructure() {
        val destinationFolder = testDirectory.newFolder("destination")
        val sourceFolder = testDirectory.newFolder("sources")
        val sourceDepthFolder = File(sourceFolder, "aaa/bbb/ccc")
        fillSourceFolder(usecase = "depth_example", target = sourceDepthFolder)

        val buildFile = testDirectory.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'com.github.janphkre.class_relations'
            }
            
            pumlGenerate {
                projectPackagePrefix = "aaa"
                selfColor = "#00FF00"
                spaceCount = 4
                generatedFileName = "example_relations.puml"
                destination = new File("${destinationFolder.absolutePath.replace('\\','/')}")
                sources = [new File("${sourceFolder.absolutePath.replace('\\','/')}")]
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testDirectory.root)
            .withPluginClasspath()
            .withArguments("generateClassRelationsPuml")
            .forwardOutput()
            .build()

        destinationFolder.printDestinationStructure()

        var child = "aaa/example_relations.puml"
        Truth.assertWithMessage(child).that(readFile(File(destinationFolder, child)))
            .isEqualTo(readFile("src/test/resources/depth_example__aaa__expected_generate_output.puml"))
        child = "aaa/bbb/example_relations.puml"
        Truth.assertWithMessage(child).that(readFile(File(destinationFolder, child)))
            .isEqualTo(readFile("src/test/resources/depth_example__aaa.bbb__expected_generate_output.puml"))
        child = "aaa/bbb/ccc/example_relations.puml"
        Truth.assertWithMessage(child).that(readFile(File(destinationFolder, child)))
            .isEqualTo(readFile("src/test/resources/depth_example__aaa.bbb.ccc__expected_generate_output.puml"))
        child = "aaa/bbb/ccc/depth_example/example_relations.puml"
        Truth.assertWithMessage(child).that(readFile(File(destinationFolder, child)))
            .isEqualTo(readFile("src/test/resources/depth_example__aaa.bbb.ccc.depth_example__expected_generate_output.puml"))
    }


    @Test
    fun actionOnDepth_skipsProjectPackagePrefix() {
        val destinationFolder = testDirectory.newFolder("destination")
        val sourceFolder = testDirectory.newFolder("sources")
        val sourceDepthFolder = File(sourceFolder, "aaa/bbb/ccc")
        fillSourceFolder(usecase = "depth_example", target = sourceDepthFolder)

        val buildFile = testDirectory.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'com.github.janphkre.class_relations'
            }
            
            pumlGenerate {
                projectPackagePrefix = "aaa.bbb.ccc"
                selfColor = "#00FF00"
                spaceCount = 4
                generatedFileName = "example_relations.puml"
                destination = new File("${destinationFolder.absolutePath.replace('\\','/')}")
                sources = [new File("${sourceFolder.absolutePath.replace('\\','/')}")]
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testDirectory.root)
            .withPluginClasspath()
            .withArguments("generateClassRelationsPuml")
            .forwardOutput()
            .build()

        destinationFolder.printDestinationStructure()

        var child = "example_relations.puml"
        Truth.assertWithMessage(child).that(File(destinationFolder, child).exists()).isFalse()
        child = "aaa/example_relations.puml"
        Truth.assertWithMessage(child).that(File(destinationFolder, child).exists()).isFalse()
        child = "aaa/bbb/example_relations.puml"
        Truth.assertWithMessage(child).that(File(destinationFolder, child).exists()).isFalse()
        child = "aaa/bbb/ccc/example_relations.puml"
        Truth.assertWithMessage(child).that(readFile(File(destinationFolder, child)))
            .isEqualTo(readFile("src/test/resources/depth_example_with_prefix__aaa.bbb.ccc__expected_generate_output.puml"))
        child = "aaa/bbb/ccc/depth_example/example_relations.puml"
        Truth.assertWithMessage(child).that(readFile(File(destinationFolder, child)))
            .isEqualTo(readFile("src/test/resources/depth_example_with_prefix__aaa.bbb.ccc.depth_example__expected_generate_output.puml"))
    }

    @Test
    fun actionOnMultipleRoots_CreatesSingleDiagram() {
        val destinationFolder = testDirectory.newFolder("destination")
        val sourceFolder1 = testDirectory.newFolder("sources1")
        val sourceFolder2 = testDirectory.newFolder("sources2")
        fillSourceFolder("multiroot_example1", "multiroot_example", sourceFolder1)
        fillSourceFolder("multiroot_example2", "multiroot_example", sourceFolder2)

        val buildFile = testDirectory.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'com.github.janphkre.class_relations'
            }
            
            pumlGenerate {
                projectPackagePrefix = "multiroot_example"
                selfColor = "#00FF00"
                spaceCount = 4
                generatedFileName = "example_relations.puml"
                destination = new File("${destinationFolder.absolutePath.replace('\\','/')}")
                sources = [
                    new File("${sourceFolder1.absolutePath.replace('\\','/')}"),
                    new File("${sourceFolder2.absolutePath.replace('\\','/')}"),
                ]
                filters = []
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testDirectory.root)
            .withPluginClasspath()
            .withArguments("generateClassRelationsPuml")
            .forwardOutput()
            .build()

        destinationFolder.printDestinationStructure()

        Truth.assertThat(readFile(File(destinationFolder, "multiroot_example/example_relations.puml")))
            .isEqualTo(readFile("src/test/resources/multiroot_example__expected_generate_output.puml"))
    }

    private fun File.printDestinationStructure() {
        println("Destination Folder content:")
        var spaceCount = 0
        this.walkTopDown()
            .onEnter {
                spaceCount++
                true
            }
            .onLeave { spaceCount-- }
            .forEach { println("${" ".repeat(spaceCount)}${it.name}") }
    }

    private fun fillSourceFolder(usecase: String, sourceDirName: String = usecase, target: File) {
        target.mkdirs()
        val usecaseFile = File(target, sourceDirName)
        usecaseFile.mkdir()
        val sourceFiles = File("src/test/resources/$usecase").listFiles()
        sourceFiles!!.forEach { source ->
            val content = source.readText().replace("\r","")
            File(usecaseFile, source.name).writeText(content)
        }
    }

    private fun readFile(filePath: String): String {
        return File(filePath).readText().replace("\r","")
    }

    private fun readFile(file: File): String {
        return file.readText().replace("\r","")
    }
}