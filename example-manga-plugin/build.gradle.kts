plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.example.komga.plugin"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Komga plugin API - provided by Komga at runtime
    compileOnly(fileTree(mapOf("dir" to "../komga/build/libs", "include" to listOf("*.jar"))))

    // For making HTTP requests in a real implementation
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    jar {
        manifest {
            attributes["Implementation-Title"] = "Manga Metadata Plugin"
            attributes["Implementation-Version"] = version
        }

        // Include the plugin descriptor
        from("komga-plugin.json") {
            into("")
        }

        // Create fat JAR with dependencies
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
