package de.janphkre.class_relations.library.model

data class KlassItem(
    val name: String,
    val filePackage: List<String>,
    val filePackageString: String,
    var isDisabled: Boolean = false
)
