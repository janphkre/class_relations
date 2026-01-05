plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("java-gradle-plugin")
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            groupId = "com.github.janphkre.class_relations"
            artifactId = "gradle_plugin"
            version = "1.0.0"

            from(components["java"])

            pom {
                name.set("class_relations Gradle Plugin")
                description.set("A gradle plugin to generate puml files out of kotlin source code")
                url.set("https://github.com/janphkre/class_relations")
            }
        }
    }
}

group = "com.github.janphkre.class_relations"
version = "1.1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    google()
}

dependencies {
    implementation(project(":library"))
    implementation("com.android.tools.build:gradle:8.13.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("io.mockk:mockk:1.14.0")
    testImplementation("ch.tutteli.atrium:atrium-fluent:1.2.0")
}

gradlePlugin {
    plugins {
        create("classRelationsGenerator") {
            id = "com.github.janphkre.class_relations"
            implementationClass = "de.janphkre.class_relations.generator.GeneratorPlugin"
        }
        create("classRelationsGeneratorGrouping") {
            id = "com.github.janphkre.class_relations.grouping"
            implementationClass = "de.janphkre.class_relations.grouping.GroupingPlugin"
        }
    }
}
