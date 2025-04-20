package de.janphkre.class_relations_viewer.model

data class KlassWithRelations(
    val item: KlassItemWithType,
    val fileImports: List<KlassItem>,
    val parameters: List<KlassItem>,
    val inheritances: List<KlassItem>,
) {
    val usages: List<KlassItem> = fileImports.filterNot { parameters.contains(it) || inheritances.contains(it) }
}