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
package de.janphkre.class_relations.library.domain

import de.janphkre.class_relations.library.model.KlassWithRelations
import java.io.Serializable

interface ClassRelationsPumlGenerator {

    data class Settings(
        val projectPackagePrefix: String,
        val selfColor: String,
        val spaceCount: Int,
        val generatedFileName: String,
        val initialCapacitySize : Int = 2048
    ): Serializable

    // All klasses must belong to the same package / folder!
    // SourcesLinks needs capitalized keys
    fun generate(
        klasses: List<KlassWithRelations>,
        childPackages: Collection<String>,
    ): String

    fun generateEmpty(filePackage: List<String>, childPackages: Collection<String>): String

    companion object {
        fun getInstance(
            settings: Settings,
            sourcesLinks: Map<String, String>,
            externalLinks: Map<String, String>,
        ): ClassRelationsPumlGenerator {
            return ClassRelationsPumlGeneratorImpl(
                settings,
                sourcesLinks,
                externalLinks
            )
        }
    }
}