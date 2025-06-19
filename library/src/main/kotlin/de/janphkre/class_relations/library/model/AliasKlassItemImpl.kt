package de.janphkre.class_relations.library.model

data class AliasKlassItemImpl(
    val delegate: KlassItem,
    override val codeIdentifier: String
): KlassItem by delegate
