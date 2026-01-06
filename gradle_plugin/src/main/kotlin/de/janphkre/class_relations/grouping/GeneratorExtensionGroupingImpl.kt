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
package de.janphkre.class_relations.grouping

import de.janphkre.class_relations.generator.GeneratorExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.File

/**
 * Configurable options for the [GeneratorPlugin] copying from the [GroupingExtension].
 */
class GeneratorExtensionGroupingImpl(
    override val projectBasePrefix: Property<String>,
    override val projectPackagePrefix: Property<String>,
    override val selfColor: Property<String>,
    override val spaceCount: Property<Int>,
    override val generatedFileName: Property<String>,
    override val destination: Property<String>,
    override val sources: ListProperty<String>,
    override val filters: ListProperty<String>,
    override val externalLinks: ListProperty<Pair<String, File>>
) : GeneratorExtension