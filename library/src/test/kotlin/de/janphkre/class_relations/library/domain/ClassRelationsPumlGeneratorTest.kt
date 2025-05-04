package de.janphkre.class_relations.library.domain

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.model.KlassItem
import de.janphkre.class_relations.library.model.KlassType
import de.janphkre.class_relations.library.model.KlassTypeData
import de.janphkre.class_relations.library.model.KlassWithRelations
import kotlinx.serialization.json.*
import org.junit.Test
import java.io.File

class ClassRelationsPumlGeneratorTest {

    @Test
    fun testBasicExample() = verifyGenerator("basic_example")

    private fun verifyGenerator(id: String) {
        val (generatorSettings, klasses) = readInput(id)
        val generator = ClassRelationsPumlGenerator.getInstance(
            generatorSettings
        )
        val result = generator.generate(
            klasses,
            rootGeneratedLink = "example/root/generated"
        )
        Truth.assertThat(result).isEqualTo(readOutput(id))
    }

    private fun readFile(file: String): String {
        return File("src/test/resources/generator/$file").readText().replace("\r","")
    }

    private fun readOutput(file: String): String {
        return readFile("$file$OUTPUT_POSTFIX")
    }

    private fun readInput(file: String): Pair<ClassRelationsPumlGenerator.Settings,List<KlassWithRelations>> {
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
                    filePath = element["filePath"]!!.jsonPrimitive.content
                ),
                fileImports = element["fileImports"]!!.jsonArray.map { it.toKlassItem()},
                parameters = element["parameters"]!!.jsonArray.map { it.toKlassItem()},
                inheritances = element["inheritances"]!!.jsonArray.map { it.toKlassItem()},
                methodParameters = element["methodParameters"]?.jsonArray?.map { it.toKlassItem() } ?: emptyList()
            )
        }
        return settings to files
    }

    private fun JsonElement.toKlassItem(): KlassItem {
        val json = this.jsonObject
        val filePackageString = json["package"]!!.jsonPrimitive.content
        return KlassItem(
            name = json["name"]!!.jsonPrimitive.content,
            filePackage = filePackageString.split("."),
            filePackageString = filePackageString
        )
    }

    companion object {
        private const val INPUT_POSTFIX = "_input.json"
        private const val OUTPUT_POSTFIX = "_result.puml"
    }
}