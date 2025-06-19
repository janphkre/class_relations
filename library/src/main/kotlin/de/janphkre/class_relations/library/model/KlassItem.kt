package de.janphkre.class_relations.library.model

interface KlassItem {
    val name: String
    val codeIdentifier: String
    val filePackage: List<String>
    val filePackageString: String
    var isDisabled: Boolean

    fun copy(filePackage: List<String>): KlassItem
}
