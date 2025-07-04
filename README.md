# class_relations

[![](https://jitpack.io/v/janphkre/class_relations.svg)](https://jitpack.io/#janphkre/class_relations)

Class_relations aims to provide a generator for class relations on a package level.

A simple example can be found in the tests:

[![](readme_generated_example.svg)](library/src/test/resources/generator/basic_example_result.puml)

## Usage

Like any gradle plugin, class_relations is also defined as a plugin:

```groovy
pluginManagement {
    repositories {
        maven { url "https://jitpack.io"  }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "de.janphkre.class_relations") {
                useModule("de.janphkre.class_relations:gradle-plugin:${classRelationsVersion}")
            }
        }
    }
}

plugins {
    id("de.janphkre.class_relations")
}


pumlGenerate {
    destination = new File(project.buildDir, "generated/puml_class_relations")
    source = new File(project.projectDir, "src")
    projectPackagePrefix = "readme.example"
    selfColor = "#00FF00"
    spaceCount = 4
    generatedFilename = "class_relations.puml"
    filters = [
            "*",
            "**.*Module"
    ]
}
```

The task `generateClassRelationsPuml` can be executed to generate the diagrams.

## License

This project is licensed under the Apache License 2.0, see [here](LICENSE).