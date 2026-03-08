pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SuledApp"
include(":app")
include(":shared")
project(":shared").projectDir = file("../../shared")
include(":wear")
project(":wear").projectDir = file("../wear")
