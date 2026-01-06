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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import de.janphkre.class_relations.generator.GeneratorPlugin
import de.janphkre.class_relations.generator.GeneratorPlugin.Companion.DEFAULT_DESTINATION
import de.janphkre.class_relations.generator.GeneratorPlugin.Companion.TASK_GROUP
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File

/**
 * Plugin to apply on a root gradle project.
 * This applies the GeneratorPlugin to all subproject.
 *
 */
class GroupingPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create(PLUGIN_NAME, GroupingExtension::class.java)
        val childrenPaths = target.subprojects.map { subProject ->
            val subPackage = subProject.getPackageOfProject(extension)
            val destination = target.layout.buildDirectory.map {
                val destination = extension.destination.orElse(DEFAULT_DESTINATION).get()
                val generatedFileName = extension.generatedFileName.get()
                val packagePath = subPackage.get().replace(".", "/")
                File(subProject.projectDir, "$destination/$packagePath/$generatedFileName")
            }
            subPackage to destination
        }
        val subTasks = target.subprojects.map { subProject ->
            val generatorPlugin = GeneratorPlugin()
            val list: Provider<List<Pair<String, File>>> = extension.externalLinks.orElse(emptyList()).map { base ->
                childrenPaths.map { (path, file) -> path.get() to file.get() }.plus(base)
            }
            val childPackage = subProject.getPackageOfProject(extension)
            val generatorExtension = GeneratorExtensionGroupingImpl(
                projectBasePrefix =  extension.projectBasePrefix,
                projectPackagePrefix = extension.projectBasePrefix,
                selfColor = extension.selfColor,
                spaceCount = extension.spaceCount,
                generatedFileName = extension.generatedFileName,
                destination = extension.destination,
                sources = extension.sources,
                filters = extension.filters,
                externalLinks = extension.externalLinks,
            )
            generatorPlugin.apply(subProject, generatorExtension, childPackage, list)//TODO: CHANGE PROJECT PACKAGE PREFIX!
        }
        target.tasks.register(GeneratorPlugin.TASK_NAME) { task ->
            task.group = TASK_GROUP
            task.setDependsOn(subTasks)
        }
    }

    private fun Project.getPackageOfProject(extension: GroupingExtension): Provider<String> {
        return providers.provider {
            val appComponents = project.extensions.findByType(AppExtension::class.java)

            if (appComponents != null) {
                return@provider appComponents.namespace
            }
            val libComponents = project.extensions.findByType(LibraryExtension::class.java)
            if (libComponents != null) {
                return@provider libComponents.namespace
            }
            extension.projectBasePrefix.orNull?.let { "${it}.${project.name}"} ?: project.name
        }
    }

    companion object {
        internal const val PLUGIN_NAME = "pumlGenerateAll"
    }
}