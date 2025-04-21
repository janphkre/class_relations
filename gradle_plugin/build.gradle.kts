plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("java-gradle-plugin")
}

group = "de.janphkre"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(project(":library"))
}

gradlePlugin {
    plugins {
        create("classRelationsGenerator") {
            id = "de.janphkre.class_relations_viewer"
            implementationClass = "de.janphkre.class_relations_viewer.generator.GeneratorPlugin"
        }
    }
}
