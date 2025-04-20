package de.janphkre.class_relations_viewer.model

data class KlassItemWithType(
    val name: String,
    val filePackage: List<String>,
    val methods: List<String>,
    val type: KlassType
)