package de.janphkre.class_relations.library.model

data class UnclearKlassItemImpl(
    override val name: String,
    override val filePackage: List<String>,
    override val filePackageString: String,
    override var isDisabled: Boolean = false
) : UnclearKlassItem {
    override fun copy(filePackage: List<String>): KlassItem {
        return UnclearKlassItemImpl(name, filePackage, filePackageString, isDisabled)
    }
}
