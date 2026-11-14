pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "NanoArray"

// ---------------------------------------------------------------------------
// Multi-version project layout
//
//   common/          -> version agnostic GUI framework (pure Java, no Minecraft)
//   versions/<mc>/   -> one Fabric Loom project per supported Minecraft version
//
// Every version module depends on :common, so a change to the shared framework
// propagates to every supported Minecraft version with a single `./gradlew buildAll`.
//
// Selecting a single version:
//   ./gradlew -PmcVersion=26.3 :versions:26.3:build
//
// Adding a new Minecraft version:
//   1. add an entry to the `nanoarray.versions` map in gradle.properties
//   2. copy versions/<existing>/ into versions/<new> and adjust its build.gradle.kts
//   3. re-run ./gradlew buildAll
// ---------------------------------------------------------------------------

val configuredVersion: String? = providers.gradleProperty("mcVersion").orNull

include("common")

val versionDirs = listOf("1.21.11", "26.1.2", "26.2", "26.3")

versionDirs.forEach { mc ->
    if (configuredVersion == null || configuredVersion == mc) {
        include(":versions:$mc")
    }
}

if (configuredVersion != null && configuredVersion !in versionDirs) {
    throw GradleException(
        "Unknown Minecraft version '$configuredVersion'. Supported: ${versionDirs.joinToString(", ")}"
    )
}
