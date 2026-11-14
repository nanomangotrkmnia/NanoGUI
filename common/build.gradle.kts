plugins {
    `java-library`
}

val commonRelease = providers.gradleProperty("nanogui.common.java.release").get().toInt()

java {
    sourceCompatibility = JavaVersion.toVersion(commonRelease)
    targetCompatibility = JavaVersion.toVersion(commonRelease)
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(commonRelease)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all,-serial,-processing", "-parameters"))
}

dependencies {
    // Gson is already on the Minecraft runtime classpath, so this adds no new
    // runtime weight to the shipped mod.
    api(libs.gson)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Point the native font loader at the binary that was built for the host
    // so that the native code path is covered by the test suite.
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    val hostOs = when {
        osName.contains("win") -> "windows"
        osName.contains("mac") || osName.contains("darwin") -> "macos"
        else -> "linux"
    }
    val hostArch = when (osArch) {
        "x86_64", "amd64" -> "x86_64"
        "aarch64", "arm64" -> "aarch64"
        else -> osArch
    }
    val nativeDirectory = rootProject.file("native/font-renderer/dist/$hostOs-$hostArch")
    if (nativeDirectory.exists()) {
        systemProperty("nanogui.native.path", nativeDirectory.absolutePath)
    }
}

/**
 * Measures the native font renderer against the Java2D fallback.
 *
 * The task prints the real numbers that are quoted in the documentation.
 */
tasks.register<JavaExec>("benchmark") {
    group = "verification"
    description = "Benchmarks the native font renderer against the Java2D fallback."
    mainClass.set("dev.nanomango.gui.test.FontBenchmark")
    classpath = sourceSets["test"].runtimeClasspath
    dependsOn(tasks.testClasses)
    jvmArgs("-Djava.awt.headless=true")
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    val hostOs = when {
        osName.contains("win") -> "windows"
        osName.contains("mac") || osName.contains("darwin") -> "macos"
        else -> "linux"
    }
    val hostArch = when (osArch) {
        "x86_64", "amd64" -> "x86_64"
        "aarch64", "arm64" -> "aarch64"
        else -> osArch
    }
    val nativeDirectory = rootProject.file("native/font-renderer/dist/$hostOs-$hostArch")
    if (nativeDirectory.exists()) {
        systemProperty("nanogui.native.path", nativeDirectory.absolutePath)
    }
}

// The native font renderer is an optional artifact: the framework falls back
// to the Java2D implementation when the library is missing. The binaries are
// produced by `native/build-native.sh` and picked up from the dist folder.
val nativeDist = rootProject.layout.projectDirectory.dir("native/font-renderer/dist")

tasks.jar {
    from(nativeDist) {
        into("dev/nanomango/native")
    }
}

