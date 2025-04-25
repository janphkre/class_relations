package de.janphkre.class_relations.library.domain

import com.google.common.truth.Truth
import de.janphkre.class_relations.library.model.KlassItem
import de.janphkre.class_relations.library.model.KlassItemWithType
import de.janphkre.class_relations.library.model.KlassType
import de.janphkre.class_relations.library.model.KlassWithRelations
import kotlinx.serialization.json.*
import org.junit.Test
import java.io.File

class ClassRelationsPumlGeneratorTest {

    private val defaultGeneratorSettings = ClassRelationsPumlGenerator.Settings(
        projectPackagePrefix = "org.example",
        selfColor = "#00FFFF",
        spaceCount = 4,
        generatedFileName = "example_relations.puml"
    )

    @Test
    fun testBasicExample() {
        val generator = ClassRelationsPumlGenerator(
            defaultGeneratorSettings
        )

        val klasses = readJson("basic_example.json")
        val result = generator.generate(
            klasses,
            rootGeneratedLink = "example/root/generated"
        )
        Truth.assertThat(result.lines()).isEqualTo(readResource("basic_example_result.puml").lines())
    }

    private fun readResource(file: String): String {
        return File("src/test/resources/$file").readText()
    }

    private fun readJson(file: String): List<KlassWithRelations> {
        val json = Json.parseToJsonElement(readResource(file))
        return json.jsonArray.map { jsonElement ->
            val element = jsonElement.jsonObject
            val elementType = element["type"]!!.jsonPrimitive.content
            KlassWithRelations(
                item = KlassItemWithType(
                    name = element["name"]!!.jsonPrimitive.content,
                    filePackage = element["filePackage"]!!.jsonPrimitive.content.split("."),
                    methods = element["methods"]!!.jsonArray.map { it.jsonPrimitive.content },
                    type = KlassType.entries.firstOrNull { it.id == elementType } ?: throw IllegalArgumentException("Type \'$elementType\' not supported"),
                    filePath = element["filePath"]!!.jsonPrimitive.content
                ),
                fileImports = element["fileImports"]!!.jsonArray.map { it.toKlassItem()},
                parameters = element["parameters"]!!.jsonArray.map { it.toKlassItem()},
                inheritances = element["inheritances"]!!.jsonArray.map { it.toKlassItem()},
            )
        }
    }

    private fun JsonElement.toKlassItem(): KlassItem {
        val json = this.jsonObject
        return KlassItem(
            name = json["name"]!!.jsonPrimitive.content,
            filePackage = json["package"]!!.toString().split(".")
        )
    }
}