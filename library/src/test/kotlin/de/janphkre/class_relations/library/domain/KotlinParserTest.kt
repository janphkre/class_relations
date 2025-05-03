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

    private fun verifyParser(id: String, filePath: String) {
        val parser = KotlinParser.getInstance()
        val result = parser.parse(readInput(id), id, filePath)
        Truth.assertThat(result).isEqualTo(readOutput(id, filePath))
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
        return KlassItem(
            name = json["name"]!!.jsonPrimitive.content,
            packageString = json["package"]!!.jsonPrimitive.content
        )
    }

    private fun readInput(file: String): String {
        return readFile("$file$INPUT_POSTFIX")
    }

    companion object {
        private const val INPUT_POSTFIX = ".kt"
        private const val OUTPUT_POSTFIX = "_result.json"
    }

}