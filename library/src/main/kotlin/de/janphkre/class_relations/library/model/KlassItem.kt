package de.janphkre.class_relations.library.model

data class KlassItem(
    val name: String,
    val filePackage: List<String>,
    val filePackageString: String
) {
    constructor(
        name: String,
        packageString: String
    ): this(
        name = name,
        filePackage = packageString.split("."),
        filePackageString = packageString
    )
    constructor(
        name: String,
        packageList: List<String>
    ): this(
        name = name,
        filePackage = packageList,
        filePackageString = packageList.joinToString(".")
    )
}
