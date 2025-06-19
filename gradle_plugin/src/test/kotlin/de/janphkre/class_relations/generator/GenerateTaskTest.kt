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
    fun action_runsThroughFiles() {
        val moduleFolder = testDirectory.newFolder("exampleModuleFolder")
        val destinationFolder = testDirectory.newFolder("destination")
        val sourceFolder = testDirectory.newFolder("sources")
        fillSourceFolder("basic_example", sourceFolder)

        val buildFile = testDirectory.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'de.janphkre.class_relations'
            }
            
            pumlGenerate {
                projectPackagePrefix = "basic_example"
                selfColor = "#00FF00"
                spaceCount = 4
                generatedFileName = "example_relations.puml"
                destination = new File("${destinationFolder.absolutePath.replace('\\','/')}")
                moduleDirectory = new File("${moduleFolder.absolutePath.replace('\\','/')}")
                source = new File("${sourceFolder.absolutePath.replace('\\','/')}")
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
    fun action_createsEmptyDiagramsForStructure() {
        val moduleFolder = testDirectory.newFolder("exampleModuleFolder")
        val destinationFolder = testDirectory.newFolder("destination")
        val sourceFolder = testDirectory.newFolder("sources")
        val sourceDepthFolder = File(sourceFolder, "aaa/bbb/ccc")
        fillSourceFolder("depth_example", sourceDepthFolder)

        val buildFile = testDirectory.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'de.janphkre.class_relations'
            }
            
            pumlGenerate {
                projectPackagePrefix = "aaa"
                selfColor = "#00FF00"
                spaceCount = 4
                generatedFileName = "example_relations.puml"
                destination = new File("${destinationFolder.absolutePath.replace('\\','/')}")
                moduleDirectory = new File("${moduleFolder.absolutePath.replace('\\','/')}")
                source = new File("${sourceFolder.absolutePath.replace('\\','/')}")
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

    private fun fillSourceFolder(usecase: String, target: File) {
        target.mkdirs()
        val usecaseFile = File(target,usecase)
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