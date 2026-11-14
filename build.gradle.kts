plugins {
    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.fabric.loom.remap) apply false
}

allprojects {
    group = providers.gradleProperty("nanoarray.group").get()
    version = providers.gradleProperty("nanoarray.version").get()
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
    }
}

/**
 * Builds every supported Minecraft version in one invocation.
 *
 * `./gradlew buildAll` produces:
 *   versions/<mc>/build/libs/nanoarray-<mc>.jar
 */
tasks.register("buildAll") {
    group = "build"
    description = "Builds NanoArray for every supported Minecraft version."
    dependsOn(subprojects.filter { it.path.startsWith(":versions:") }.map { "${it.path}:build" })
}

/**
 * Runs the shared framework unit tests for every supported Minecraft version.
 */
tasks.register("testAll") {
    group = "verification"
    description = "Runs the NanoArray test suite for every supported Minecraft version."
    dependsOn(subprojects.filter { it.path.startsWith(":versions:") }.map { "${it.path}:test" })
}

/**
 * Builds the native font renderer for every supported platform.
 *
 * The task requires a Rust toolchain and Zig, which is only needed by
 * developers. The released jars already contain the compiled libraries, so
 * end users never need a compiler.
 */
tasks.register<Exec>("buildNative") {
    group = "native"
    description = "Cross compiles the native font renderer for every supported platform."
    workingDir = rootProject.file("native")
    commandLine("bash", "build-native.sh")
    isIgnoreExitValue = false
}

/**
 * Verifies that the native font renderer was built for every platform.
 */
tasks.register("checkNative") {
    group = "verification"
    description = "Checks that the native font renderer exists for every supported platform."
    val dist = rootProject.layout.projectDirectory.dir("native/font-renderer/dist")
    val expected = listOf(
        "macos-x86_64/libnanoarray_font.dylib",
        "macos-aarch64/libnanoarray_font.dylib",
        "windows-x86_64/nanoarray_font.dll",
        "windows-aarch64/nanoarray_font.dll",
        "linux-x86_64/libnanoarray_font.so",
        "linux-aarch64/libnanoarray_font.so",
    )
    doLast {
        val missing = expected.filter { !dist.file(it).asFile.exists() }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "The native font renderer is missing for: ${missing.joinToString(", ")}.\n" +
                    "Run ./gradlew buildNative to build it."
            )
        }
        logger.lifecycle("Native font renderer found for all ${expected.size} platforms.")
    }
}

