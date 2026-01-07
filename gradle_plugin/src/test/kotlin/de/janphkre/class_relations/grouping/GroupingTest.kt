package de.janphkre.class_relations.grouping

import com.google.common.truth.Truth
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GroupingTest {

    @get:Rule
    val testDirectory = TemporaryFolder()

    @Test
    fun multiModuleProject_groupingPluginApplied_generatesDiagramsForAllModules() {
        val submoduleAFolder = testDirectory.newFolder("submodule_a")
        val submoduleBFolder = testDirectory.newFolder("submodule_b")
        val submoduleASources = File(submoduleAFolder, "sources")
        val submoduleBSources = File(submoduleBFolder, "sources")
        fillSourceFolder(usecase = "submodule_a", sourceDirName = "multi_module_example/submodule_a", target = submoduleASources)
        fillSourceFolder(usecase = "submodule_b", sourceDirName = "multi_module_example/submodule_b", target = submoduleBSources)

        val buildFile = testDirectory.newFile("build.gradle")
        buildFile.writeText("""
            plugins {
                id 'com.github.janphkre.class_relations.grouping'
            }
            
            pumlGenerateAll {
                projectBasePrefix = "multi_module_example"
                selfColor = "#00FF00"
                spaceCount = 4
                generatedFileName = "example_multi.puml"
                destination = "destination"
                sources = ["sources"]
                filters = [
                    "io.other.UsageClassD",
                    "com.example.b.ParameterClassC"
                ]
            }
        """.trimIndent())
        val settingsFile = testDirectory.newFile("settings.gradle")
        settingsFile.writeText("""
            include ':submodule_a'
            include ':submodule_b'
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testDirectory.root)
            .withPluginClasspath()
            .withArguments("generateClassRelationsPuml", "--stacktrace")
            .withDebug(true)
            .forwardOutput()
            .build()

        val destinationFolderA = File(submoduleAFolder, "destination")
        destinationFolderA.printDestinationStructure()
        val destinationFolderB = File(submoduleBFolder, "destination")
        destinationFolderB.printDestinationStructure()

        Truth.assertThat(readFile(File(destinationFolderA, "multi_module_example/submodule_a/example_multi.puml")))
            .isEqualTo(readFile("src/test/resources/grouping/submodule_a__expected_generate_output.puml"))

        Truth.assertThat(readFile(File(destinationFolderB, "multi_module_example/submodule_b/example_multi.puml")))
            .isEqualTo(readFile("src/test/resources/grouping/submodule_b__expected_generate_output.puml"))
    }

    @Test
    fun fullAndroidMultiModuleProject_groupingPluginApplied_generatesDiagramsForAllModules() {
        val submoduleAFolder = testDirectory.newFolder("submodule_app")
        val submoduleBFolder = testDirectory.newFolder("submodule_library")
        fillSourceFolder(usecase = "submodule_android_app", sourceDirName = "src/main/java/multi_module_example/android/example/submodule_app", target = submoduleAFolder)
        fillSourceFolder(usecase = "submodule_android_library", sourceDirName = "src/main/java/multi_module_example/android/example/submodule_library", target = submoduleBFolder)
        fillSourceFolder(usecase = "submodule_android_library_subpackage", sourceDirName = "src/main/java/multi_module_example/android/example/submodule_library/subpackage", target = submoduleBFolder)
        fillSourceFolder(usecase = "submodule_android_library_subpackage_adjacent", sourceDirName = "src/main/java/multi_module_example/android/example/submodule_library/adjacent", target = submoduleBFolder)

        val buildFile = testDirectory.newFile("build.gradle")
        buildFile.writeText("""
            plugins {
                id 'com.github.janphkre.class_relations.grouping'
            }
            
            pumlGenerateAll {
                projectBasePrefix = "multi_module_example.android.example"
                selfColor = "#00FF00"
                spaceCount = 4
                generatedFileName = "example_multi.puml"
                destination = "destination/generated/class_relations"
                sources = ["src/main/java"]
                filters = [
                    "io.other.UsageClassD",
                    "com.example.b.ParameterClassC"
                ]
            }
        """.trimIndent())
        val settingsFile = testDirectory.newFile("settings.gradle")
        settingsFile.writeText("""
            include ':submodule_app'
            include ':submodule_library'
        """.trimIndent())


        val submoduleABuildFile = File(submoduleAFolder, "build.gradle")
        submoduleABuildFile.writeText("""
              plugins {
                  id 'com.android.application'
              }
              
              android {
                  namespace = "multi_module_example.android.example.submodule_app"
                  compileSdk = 36
              }
        """.trimIndent())
        val submoduleBBuildFile = File(submoduleBFolder, "build.gradle")
        submoduleBBuildFile.writeText("""
              plugins {
                  id 'com.android.library'
              }
              
              android {
                  namespace = "multi_module_example.android.example.submodule_library"
                  compileSdk = 36
              }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testDirectory.root)
            .withPluginClasspath()
            .withArguments("generateClassRelationsPuml", "--stacktrace")
            .withDebug(true)
            .forwardOutput()
            .build()

        val destinationFolderA = File(submoduleAFolder, "destination")
        destinationFolderA.printDestinationStructure()
        val destinationFolderB = File(submoduleBFolder, "destination")
        destinationFolderB.printDestinationStructure()

        Truth.assertThat(readFile(File(destinationFolderA, "generated/class_relations/multi_module_example/android/example/submodule_app/example_multi.puml")))
            .isEqualTo(readFile("src/test/resources/grouping/submodule_android_app__expected_generate_output.puml"))

        Truth.assertThat(readFile(File(destinationFolderB, "generated/class_relations/multi_module_example/android/example/submodule_library/example_multi.puml")))
            .isEqualTo(readFile("src/test/resources/grouping/submodule_android_library__expected_generate_output.puml"))

        Truth.assertThat(readFile(File(destinationFolderB, "generated/class_relations/multi_module_example/android/example/submodule_library/subpackage/example_multi.puml")))
            .isEqualTo(readFile("src/test/resources/grouping/submodule_android_library_subpackage__expected_generate_output.puml"))

        Truth.assertThat(readFile(File(destinationFolderB, "generated/class_relations/multi_module_example/android/example/submodule_library/adjacent/example_multi.puml")))
            .isEqualTo(readFile("src/test/resources/grouping/submodule_android_library_subpackage_adjacent__expected_generate_output.puml"))
    }

    private fun File.printDestinationStructure() {
        println("Destination Folder (${this.absolutePath}) content:")
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
        usecaseFile.mkdirs()
        val sourceFiles = File("src/test/resources/grouping/$usecase").listFiles()
        sourceFiles!!.forEach { source ->
            val content = source.readText().replace("\r","")
            File(usecaseFile, source.name).writeText(content)
        }
    }

    private fun readFile(filePath: String): String {
        return readFile(File(filePath))
    }

    private fun readFile(file: File): String {
        return file.readText().replace("\r","")
    }
}