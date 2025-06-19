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
}
...
plugins {
    id("de.janphkre.class_relations") version "${currentlyReleasedVersion}"
}
```
## License

This project is licensed under the Apache License 2.0, see [here](LICENSE).