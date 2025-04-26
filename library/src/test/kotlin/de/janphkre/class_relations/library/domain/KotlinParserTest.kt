package de.janphkre.class_relations.library.domain

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.model.KlassDefinition
import de.janphkre.class_relations.library.model.KlassType
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

    private fun verifyParser(id: String, filePath: String) {
        val parser = KotlinParser.getInstance()
        val result = parser.parse(readInput(id), id, filePath)
        Truth.assertThat(result).isEqualTo(readOutput(id, filePath))
    }

    private fun readFile(file: String): String {
        return File("src/test/resources/parser/$file").readText().replace("\r","")
    }

    private fun readOutput(file: String, filePath: String): KlassDefinition? {
        val json = Json.parseToJsonElement(readFile("$file$OUTPUT_POSTFIX"))
        if (json is JsonNull) {
            return null
        }
        val jsonObject = json.jsonObject
        val elementType = jsonObject["type"]!!.jsonPrimitive.content
        return KlassDefinition(
            name = jsonObject["name"]!!.jsonPrimitive.content,
            filePackage = jsonObject["filePackage"]!!.jsonPrimitive.content.split("."),
            classType = KlassType.entries.firstOrNull { it.id == elementType } ?: throw IllegalArgumentException("Type \'$elementType\' not supported"),
            fileImports = jsonObject["fileImports"]!!.jsonArray.map { it.jsonPrimitive.content.split(".") },
            parameters = jsonObject["parameters"]!!.jsonArray.map { it.jsonPrimitive.content },
            inheritances = jsonObject["inheritances"]!!.jsonArray.map { it.jsonPrimitive.content },
            methods = jsonObject["methods"]!!.jsonArray.map { it.jsonPrimitive.content },
            filePath = filePath
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