pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "class_relations"

include("idea_plugin")
include("gradle_plugin")
include("library")