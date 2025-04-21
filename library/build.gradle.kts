plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
}

group = "de.janphkre"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.kotlinx.ast:grammar-kotlin-parser-antlr-kotlin-jvm:0.1.0")
}
