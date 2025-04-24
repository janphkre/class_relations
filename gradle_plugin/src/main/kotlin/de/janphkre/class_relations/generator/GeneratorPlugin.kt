package de.janphkre.class_relations.generator

import de.janphkre.class_relations.library.domain.ClassRelationsPumlGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class GeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("pumlGenerate", GeneratorExtension::class.java)

        target.tasks.register("generateClassRelationsPuml", GenerateTask::class.java) { task ->
            task.group = "documentation"
            task.moduleDirectory.set(extension.moduleDirectory.convention(target.rootDir))
            task.destination.set(extension.destination.convention(File(target.buildFile, "generated/puml_class_relations")))
            task.source.set(extension.source.convention(File(target.rootDir, "src")))
            val compositeSettings = extension.projectPackagePrefix.flatMap { prefix ->
                extension.selfColor.flatMap { color ->
                    extension.generatedFileName.flatMap { fileName ->
                        extension.spaceCount.map { spaceCount ->
                            ClassRelationsPumlGenerator.Settings(
                                projectPackagePrefix = prefix,
                                selfColor = color,
                                spaceCount = spaceCount,
                                generatedFileName = fileName
                            )
                        }
                    }
                }

            }
            task.generatorSettings.set(compositeSettings)
        }
    }
}