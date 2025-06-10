package de.janphkre.class_relations.generator.filetree

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.data.item.KlassItemFactory
import de.janphkre.class_relations.library.domain.ClassRelationsPumlGenerator
import de.janphkre.class_relations.library.model.KlassItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.*
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GenerateTaskTest {

    @get:Rule
    val testDirectory = TemporaryFolder()

    private val klassItemFactory = KlassItemFactory.getInstance()

    @Test
    fun action_runsThroughFiles() {
        val settings = ClassRelationsPumlGenerator.Settings(
            generatedFileName = "example_relations.puml",
            projectPackagePrefix = "basic_example",
            spaceCount = 4,
            selfColor = "#00FF00"
        )
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

        Truth.assertThat(readFile(File(destinationFolder, "basic_example/example_relations.puml")))
            .isEqualTo(readFile("src/test/resources/expected_generate_output.puml"))
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

    private fun JsonElement.toKlassItem(): KlassItem {
        val json = this.jsonObject
        val filePackageString = json["package"]!!.jsonPrimitive.content
        return klassItemFactory.createItem(
            name = json["name"]!!.jsonPrimitive.content,
            packageString = filePackageString
        )
    }
}