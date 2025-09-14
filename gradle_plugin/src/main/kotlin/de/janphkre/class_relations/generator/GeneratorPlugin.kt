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

import de.janphkre.class_relations.library.domain.ClassRelationsPumlGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class GeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create(PLUGIN_NAME, GeneratorExtension::class.java)

        target.tasks.register(TASK_NAME, GenerateTask::class.java) { task ->
            task.group = TASK_GROUP
            task.destination.set(extension.destination.convention(
                target.layout.buildDirectory.map { File(it.asFile, DEFAULT_DESTINATION) })
            )
            val inputSources = extension.sources.convention(
                listOf(File(target.projectDir, DEFAULT_SOURCE))
            )
            task.sources.set(inputSources)
            for (input in inputSources.get()) {
                task.inputs.dir(input)
            }
            task.filters.set(extension.filters.convention(emptyList()))
            val compositeSettings = extension.projectBasePrefix
                .convention(extension.projectPackagePrefix)
                .flatMap { basePrefix ->
                    extension.projectPackagePrefix.flatMap { packagePrefix ->
                        extension.selfColor.flatMap { color ->
                            extension.generatedFileName.flatMap { fileName ->
                                extension.spaceCount.map { spaceCount ->
                                    ClassRelationsPumlGenerator.Settings(
                                        projectBasePrefix = basePrefix,
                                        projectPackagePrefix = packagePrefix,
                                        selfColor = color,
                                        spaceCount = spaceCount,
                                        generatedFileName = fileName
                                    )
                                }
                            }
                        }
                    }
                }
            task.generatorSettings.set(compositeSettings)
            task.externalLinks.set(extension.externalLinks.convention(emptyList()))
        }
    }

    private companion object {
        private const val PLUGIN_NAME = "pumlGenerate"
        private const val TASK_NAME = "generateClassRelationsPuml"
        private const val TASK_GROUP = "documentation"
        private const val DEFAULT_DESTINATION = "generated/puml_class_relations"
        private const val DEFAULT_SOURCE = "src/main/kotlin"
    }
}