package de.janphkre.class_relations.generator

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File

interface GeneratorExtension {
    val projectPackagePrefix: Property<String>
    val selfColor: Property<String>
    val spaceCount: Property<Int>
    val generatedFileName: Property<String>
    val destination: Property<File>
    val moduleDirectory: Property<File>
    val source: Property<File>
    val filters: ListProperty<String>
}