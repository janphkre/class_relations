/**
 *    Copyright 2025 Jan Phillip Kretzschmar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.janphkre.class_relations.generator

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File

/**
 * Configurable options for the [GeneratorPlugin].
 */
interface GeneratorExtension {
    /**
     * The project package prefix as in the base package where the project / module is defined in.
     * Most of the time this may be the library id.
     */
    val projectPackagePrefix: Property<String>

    /**
     * Set the color of the currently displayed package.
     * Helps to identify which package is in the focus of the current diagram.
     * Expects a Hex-RGB string.
     */
    val selfColor: Property<String>

    /**
     * Set the space count of the resulting plant-UML indentation.
     * Expects a positive integer or zero.
     */
    val spaceCount: Property<Int>

    /**
     * Set the filename of the output files created in the destination.
     * Example: "class_relations.puml"
     */
    val generatedFileName: Property<String>

    /**
     * Set the destination folder in which the puml file structure shall be created.
     * Defaults to "build/generated/puml_class_relations".
     */
    val destination: Property<File>

    /**
     * Set the source folder of the project of which puml files shall be created.
     * Defaults to "src".
     */
    val source: Property<File>

    /**
     * Define a set of filters that can be used to exclude files or relations
     * to be excluded from the generated puml diagrams.
     * Accepts import like statements like "de.example.MyClass".
     * "*" and "**" are accepted as in a glob.
     */
    val filters: ListProperty<String>
}