package de.janphkre.class_relations_viewer.library.model

data class KlassDefinition(
    val name: String,
    val filePackage: List<String>,
    val classType: KlassType,
    val fileImports: List<List<String>>,
    val parameters: List<String>,
    val inheritances: List<String>,
    val methods: List<String>
) {

    fun toKlassWithRelations(): KlassWithRelations {
        val fileImportItems = fileImports.map { KlassItem(it.last(), it.dropLast(1)) }
        return KlassWithRelations(
            item = KlassItemWithType(
                name = name,
                filePackage = filePackage,
                type = classType,
                methods = methods
            ),
            fileImports = fileImportItems,
            parameters = parameters.map { parameter -> fileImportItems.firstOrNull { it.name == parameter } ?: KlassItem(parameter, filePackage) }, //TODO: SUPPORT ALIAS IMPORTS
            inheritances = inheritances.map { inheritance -> fileImportItems.firstOrNull { it.name == inheritance } ?: KlassItem(inheritance, filePackage) }, //TODO: SUPPORT ALIAS IMPORTS,
        )
    }
}