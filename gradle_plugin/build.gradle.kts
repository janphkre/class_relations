plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("java-gradle-plugin")
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.janphkre.class_relations"
            artifactId = "gradle-plugin"
            version = "1.0.0"

            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(project(":library"))
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("io.mockk:mockk:1.14.0")
    testImplementation("ch.tutteli.atrium:atrium-fluent:1.2.0")
}

gradlePlugin {
    plugins {
        create("classRelationsGenerator") {
            id = "de.janphkre.class_relations"
            implementationClass = "de.janphkre.class_relations.generator.GeneratorPlugin"
        }
    }
}
