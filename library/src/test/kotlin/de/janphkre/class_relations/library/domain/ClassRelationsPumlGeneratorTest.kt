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
package de.janphkre.class_relations.library.domain

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.data.item.KlassItemFactory
import de.janphkre.class_relations.library.model.KlassItem
import de.janphkre.class_relations.library.model.KlassType
import de.janphkre.class_relations.library.model.KlassTypeData
import de.janphkre.class_relations.library.model.KlassWithRelations
import kotlinx.serialization.json.*
import org.junit.Test
import java.io.File

class ClassRelationsPumlGeneratorTest {

    private val klassItemFactory = KlassItemFactory.getInstance()

    @Test
    fun testBasicExample() = verifyGenerator("basic_example")

    @Test
    fun testNestedPackagesExample() = verifyGenerator("nested_packages")

    @Test
    fun testEmptyPackagesExample() = verifyGeneratorEmpty("empty_packages")

    private fun verifyGenerator(id: String) {
        val (generatorSettings, klasses, packages) = readInput(id)
        val generator = ClassRelationsPumlGenerator.getInstance(
            generatorSettings
        )
        val result = generator.generate(
            klasses = klasses,
            childPackages = packages,
            sourcesLinks = mapOf("ExampleRootGenerated" to "example/root/generated")
        )
        Truth.assertThat(result).isEqualTo(readOutput(id))
    }

    private fun verifyGeneratorEmpty(id: String) {
        val (generatorSettings, klasses, packages) = readInput(id)
        val generator = ClassRelationsPumlGenerator.getInstance(
            generatorSettings
        )
        val result = generator.generateEmpty(
            filePackage = klasses.first().item.filePackage,
            childPackages = packages
        )
        Truth.assertThat(result).isEqualTo(readOutput(id))
    }

    private fun readFile(file: String): String {
        return File("src/test/resources/generator/$file").readText().replace("\r","")
    }

    private fun readOutput(file: String): String {
        return readFile("$file$OUTPUT_POSTFIX")
    }

    private fun readInput(file: String): Triple<ClassRelationsPumlGenerator.Settings,List<KlassWithRelations>, List<String>> {
        val json = Json.parseToJsonElement(readFile("$file$INPUT_POSTFIX")).jsonObject
        val jsonSettings = json["generatorSettings"]!!.jsonObject
        val settings = ClassRelationsPumlGenerator.Settings(
            projectPackagePrefix = jsonSettings["projectPackagePrefix"]!!.jsonPrimitive.content,
            selfColor = jsonSettings["selfColor"]!!.jsonPrimitive.content,
            spaceCount = jsonSettings["spaceCount"]!!.jsonPrimitive.int,
            generatedFileName = jsonSettings["generatedFileName"]!!.jsonPrimitive.content,
        )
        val files = json["files"]!!.jsonArray.map { jsonElement ->
            val element = jsonElement.jsonObject
            val elementType = element["type"]!!.jsonPrimitive.content
            KlassWithRelations(
                item = element.toKlassItem(),
                type = KlassTypeData(
                    methods = element["methods"]!!.jsonArray.map { it.jsonPrimitive.content },
                    type = KlassType.entries.firstOrNull { it.id == elementType } ?: throw IllegalArgumentException("Type \'$elementType\' not supported"),
                    filePath = element["filePath"]!!.jsonPrimitive.content,
                    codeBaseName = "ExampleRootGenerated"
                ),
                fileImports = element["fileImports"]!!.jsonArray.map { it.toKlassItem()},
                parameters = element["parameters"]!!.jsonArray.map { it.toKlassItem()},
                inheritances = element["inheritances"]!!.jsonArray.map { it.toKlassItem()},
                methodParameters = element["methodParameters"]?.jsonArray?.map { it.toKlassItem() } ?: emptyList()
            )
        }
        val packages = json["packages"]?.jsonArray?.map { jsonElement ->
            val element = jsonElement.jsonObject
            element["name"]!!.jsonPrimitive.content
        } ?: emptyList()
        return Triple(settings, files, packages)
    }

    private fun JsonElement.toKlassItem(): KlassItem {
        val json = this.jsonObject
        val filePackageList = json["package"]!!.jsonPrimitive.content.split(".")
        return klassItemFactory.createItem(
            name = json["name"]!!.jsonPrimitive.content,
            packageList = filePackageList
        )
    }

    companion object {
        private const val INPUT_POSTFIX = "_input.json"
        private const val OUTPUT_POSTFIX = "_result.puml"
    }
}