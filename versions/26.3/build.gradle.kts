plugins {
    id("net.fabricmc.fabric-loom")
}

val mcVersion = providers.gradleProperty("minecraft.26_3.version").get()
val javaRelease = providers.gradleProperty("minecraft.26_3.java").get().toInt()
val fabricApi = providers.gradleProperty("minecraft.26_3.fabricApi").get()
val loaderVersion = providers.gradleProperty("loader.version").get()
val lwjglVersion = providers.gradleProperty("minecraft.26_3.lwjgl").get()

version = "${project.version}+mc${mcVersion}"

loom {
    splitEnvironmentSourceSets()

    val nanoArrayMod = mods.create("nanoarray")
    nanoArrayMod.sourceSet(sourceSets.getByName("main"))
    nanoArrayMod.sourceSet(sourceSets.getByName("client"))
}


// Shared Minecraft integration code. The `shared` directory contains the
// parts that are identical on every Minecraft version, `shared-26x` the parts
// that only exist on the 26.x line.
sourceSets {
    named("client") {
        java.srcDir(rootProject.file("versions/shared/src/client/java"))
    java.srcDir(rootProject.file("versions/shared-26x/src/client/java"))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")

    implementation("net.fabricmc:fabric-loader:$loaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApi")

    // Shared, Minecraft agnostic GUI framework. The classes are flattened
    // into the mod jar below so that the mod stays a single file.
    compileOnly(project(":common"))

    // Optional NanoVG vector backend. The LWJGL version must match the one
    // Minecraft ships, otherwise two incompatible native libraries end up on
    // the classpath.
    compileOnly("org.lwjgl:lwjgl-nanovg:$lwjglVersion")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaRelease)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all,-serial,-processing", "-parameters"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaRelease)
    targetCompatibility = JavaVersion.toVersion(javaRelease)
    withSourcesJar()
}

val resourceProps = mapOf("version" to project.version, "minecraft" to mcVersion)

tasks.processResources {
    inputs.properties(resourceProps)
    filesMatching("fabric.mod.json") {
        expand(resourceProps)
    }
}

tasks.jar {
    from(rootProject.file("LICENSE"))

    // Flatten the shared framework into the mod jar.
    from(provider {
        zipTree(project(":common").tasks.named<Jar>("jar").get().archiveFile)
    }) {
        exclude("META-INF/**")
    }
}
