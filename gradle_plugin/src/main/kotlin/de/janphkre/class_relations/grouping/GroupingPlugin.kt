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
import de.janphkre.class_relations.generator.GeneratorExtension
import de.janphkre.class_relations.generator.GeneratorPlugin
import de.janphkre.class_relations.generator.GeneratorPlugin.Companion.DEFAULT_DESTINATION
import de.janphkre.class_relations.generator.GeneratorPlugin.Companion.PLUGIN_NAME
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
        val extension = target.extensions.create(PLUGIN_NAME, GeneratorExtension::class.java)
        val childrenPaths = target.subprojects.map { subProject ->
            val destination = target.layout.buildDirectory.map {
                File(it.asFile, extension.destination.convention(DEFAULT_DESTINATION).get())
            }
            subProject.getPackageOfProject() to destination
        }
        target.subprojects.map { subProject ->
            val generatorPlugin = GeneratorPlugin()
            val list: Provider<List<Pair<String, File>>> = extension.externalLinks.map { base ->
                childrenPaths.map { (path, file) -> path.get() to file.get() }.plus(base)
            }
            generatorPlugin.apply(subProject, extension, list)
        }
    }

    private fun Project.getPackageOfProject(): Provider<String> {
        return providers.provider {
            val appComponents = project.extensions.findByType(AppExtension::class.java)

            if (appComponents != null) {
                return@provider appComponents.namespace
            }
            val libComponents = project.extensions.findByType(LibraryExtension::class.java)
            if (libComponents != null) {
                return@provider libComponents.namespace
            }
            null
        }
    }
}