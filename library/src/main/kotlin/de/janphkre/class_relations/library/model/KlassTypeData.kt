package de.janphkre.class_relations.library.model

data class KlassTypeData(
    val methods: List<String>,
    val type: KlassType,
    val filePath: String
)