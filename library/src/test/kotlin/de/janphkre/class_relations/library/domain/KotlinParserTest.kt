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
import de.janphkre.class_relations.library.model.*
import kotlinx.serialization.json.*
import java.io.File
import kotlin.test.Test

internal class KotlinParserTest {

    @Test
    fun testBasicClass() = verifyParser("BasicClass", "example/file/path")

    @Test
    fun testClassWithMethods() = verifyParser("ClassWithMethods", "example/file/path2")

    @Test
    fun testDataClassWithParameters() = verifyParser("DataClassWithParameters", "example/file/path3")

    @Test
    fun testInterfaceWithMethods() = verifyParser("InterfaceWithMethods", "example/file/path4")

    @Test
    fun testObjectWithInheritances() = verifyParser("ObjectWithInheritances", "example/file/path5")

    @Test
    fun testAbstractClass() = verifyParser("AbstractClass", "example/file/path6")

    @Test
    fun testEnumClass() = verifyParser("EnumClass", "example/file/path7")

    @Test
    fun testClassWithDistinctDependencies() = verifyParser("ClassWithDistinctDeps", "example/file/path8")

    @Test
    fun testClassWithUsages() = verifyParser("ClassWithUsages", "example/file/path9")

    @Test
    fun testClassWithAliasDeps() = verifyParser("ClassWithAliasDeps", "example/file/path10")

    private fun verifyParser(id: String, filePath: String) {
        val parser = KotlinParser.getInstance()
        val result = parser.parse(readInput(id), id, filePath)
        val expected = readOutput(id, filePath)
        Truth.assertThat(result).isEqualTo(expected)
    }

    private fun readFile(file: String): String {
        return File("src/test/resources/parser/$file").readText().replace("\r","")
    }

    private fun readOutput(file: String, filePath: String): KlassWithRelations? {
        val json = Json.parseToJsonElement(readFile("$file$OUTPUT_POSTFIX"))
        if (json is JsonNull) {
            return null
        }
        val jsonObject = json.jsonObject
        val elementType = jsonObject["type"]!!.jsonPrimitive.content
        return KlassWithRelations(
            item = jsonObject.toKlassItem(),
            type = KlassTypeData(
                type = KlassType.entries.firstOrNull { it.id == elementType } ?: throw IllegalArgumentException("Type \'$elementType\' not supported"),
                methods = jsonObject["methods"]!!.jsonArray.map { it.jsonPrimitive.content },
                filePath = filePath
            ),
            fileImports = jsonObject["fileImports"]!!.jsonArray.map { it.toKlassItem() },
            parameters = jsonObject["parameters"]!!.jsonArray.map { it.toKlassItem() },
            inheritances = jsonObject["inheritances"]!!.jsonArray.map { it.toKlassItem() },
            methodParameters = jsonObject["methodParameters"]?.jsonArray?.map { it.toKlassItem() } ?: emptyList()
        )
    }

    private fun JsonElement.toKlassItem(): KlassItem {
        val json = this.jsonObject
        val filePackageString = json["package"]!!.jsonPrimitive.content
        val name = json["name"]!!.jsonPrimitive.content
        val alias = json["alias"]?.jsonPrimitive?.content
        val item = if (filePackageString == "") {
            UnclearKlassItemImpl(
                name = name,
                filePackage = emptyList(),
                filePackageString = ""
            )
        } else {
            KlassItemImpl(
                name = name,
                filePackage = filePackageString.split("."),
                filePackageString = filePackageString
            )
        }
        if (alias != null) {
            return AliasKlassItemImpl(
                delegate = item,
                codeIdentifier = alias
            )
        }
        return item
    }

    private fun readInput(file: String): String {
        return readFile("$file$INPUT_POSTFIX")
    }

    companion object {
        private const val INPUT_POSTFIX = ".kt"
        private const val OUTPUT_POSTFIX = "_result.json"
    }
//TODO: VERIFY SAME INSTANCE IS CREATED IN KOTLIN PARSER FOR ALL KLASS ITEMS
}