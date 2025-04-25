plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
}

group = "de.janphkre"
version = "1.0-SNAPSHOT"

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
