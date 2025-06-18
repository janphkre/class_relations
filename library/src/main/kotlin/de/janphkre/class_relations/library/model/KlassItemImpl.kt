package de.janphkre.class_relations.library.model

data class KlassItemImpl(
    override val name: String,
    override val filePackage: List<String>,
    override val filePackageString: String,
    override var isDisabled: Boolean = false
): KlassItem {
    override fun copy(filePackage: List<String>): KlassItem {
        return KlassItemImpl(name, filePackage, filePackageString, isDisabled)
    }
}
