plugins {
    id("com.gradleup.shadow") version "8.3.6" // Import shadow API.
    java // Tell gradle this is a java project.
    eclipse // Import eclipse plugin for IDE integration.
    kotlin("jvm") version "2.1.21" // Import kotlin jvm plugin for kotlin/java integration.
}

java {
    // Declare java version.
    sourceCompatibility = JavaVersion.VERSION_17
}

group = "net.trueog.firstplayed-og" // Declare bundle identifier.
version = "3.0" // Declare plugin version (will be in .jar).
val apiVersion = "1.19" // Declare minecraft server target version.

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to version,
        "apiVersion" to apiVersion
    )

    inputs.properties(props) // Indicates to rerun if version changes.

    filesMatching("plugin.yml") {
        expand(props)
    }
    from("LICENSE") { // Bundle license into .jars.
        into("/")
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
    }
    maven {
    	url = uri("https://jitpack.io")
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT") // Declare purpur API version to be packaged.
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3") // Import MiniPlaceholders API.
    implementation("io.lettuce:lettuce-core:6.7.1.RELEASE")
    compileOnly(project(":libs:Utilities-OG"))
}

tasks.withType<AbstractArchiveTask>().configureEach { // Ensure reproducible .jars
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    relocate("io.lettuce", "net.trueog.shadow.io.lettuce")
    exclude("io.github.miniplaceholders.*") // Exclude the MiniPlaceholders package from being shadowed.
    archiveClassifier.set("") // Use empty string instead of null.
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier.set("part")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation") // Triggers deprecation warning messages.
    options.encoding = "UTF-8"
    options.isFork = true
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}
