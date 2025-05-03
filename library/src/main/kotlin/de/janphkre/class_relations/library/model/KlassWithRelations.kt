package de.janphkre.class_relations.library.model

data class KlassWithRelations(
    val item: KlassItem,
    val type: KlassTypeData,
    val fileImports: List<KlassItem>,
    val parameters: List<KlassItem>,
    val inheritances: List<KlassItem>,
    val methodParameters: List<KlassItem>
) {

    val usages: List<KlassItem>
        get() = fileImports.plus(methodParameters).filterNot { parameters.contains(it) || inheritances.contains(it) }
        .distinct()
}