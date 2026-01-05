plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("java-gradle-plugin")
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("library") {
            groupId = "com.github.janphkre.class_relations"
            artifactId = "library"
            version = "1.1.0"

            from(components["java"])

            pom {
                name.set("class_relations Library")
                description.set("A library to generate puml out of kotlin sources")
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
}

dependencies {
    implementation("com.github.kotlinx.ast:grammar-kotlin-parser-antlr-kotlin-jvm:0.1.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation(kotlin("stdlib"))
}
