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
package de.janphkre.class_relations.library.model

data class KlassItemImpl(
    override val name: String,
    override val filePackage: List<String>,
    override val filePackageString: String,
    override var isDisabled: Boolean = false
): KlassItem {

    override val codeIdentifier: String
        get() = name

    override fun copy(filePackage: List<String>): KlassItem {
        return KlassItemImpl(
            name = name,
            filePackage = filePackage,
            filePackageString = filePackageString,
            isDisabled = isDisabled
        )
    }
}
