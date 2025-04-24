package de.janphkre.class_relations.library.model

data class KlassItemWithType(
    val name: String,
    val filePackage: List<String>,
    val methods: List<String>,
    val type: KlassType,
    val filePath: String
)