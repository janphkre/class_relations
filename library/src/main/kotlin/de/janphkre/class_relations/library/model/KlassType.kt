package de.janphkre.class_relations.library.model


enum class KlassType(val id: String) {
    DATA_CLASS("data class"), CLASS("class"), ABSTRACT_CLASS("abstract class"), OBJECT("object"), INTERFACE("interface"), ENUM_CLASS("enum clas"), UNKNOWN("")
}