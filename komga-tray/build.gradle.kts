import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  run {
    kotlin("jvm")
    kotlin("plugin.spring")
  }
  alias(libs.plugins.gradleGitProperties)
  alias(libs.plugins.compose)
  id("org.jetbrains.kotlin.plugin.compose") version "2.4.10"
  id("dev.hydraulic.conveyor") version "1.12"
  application
}

group = "org.gotson"

repositories {
  mavenCentral()
  google()
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
  }
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
}

dependencies {
  implementation(project(":komga"))

  implementation(compose.desktop.currentOs)
  implementation("org.jetbrains.compose.components:components-resources:${libs.versions.compose.get()}")

  linuxAmd64("org.jetbrains.compose.desktop:desktop-jvm-linux-x64:${libs.versions.compose.get()}")
  macAmd64("org.jetbrains.compose.desktop:desktop-jvm-macos-x64:${libs.versions.compose.get()}")
  macAarch64("org.jetbrains.compose.desktop:desktop-jvm-macos-arm64:${libs.versions.compose.get()}")
  windowsAmd64("org.jetbrains.compose.desktop:desktop-jvm-windows-x64:${libs.versions.compose.get()}")
}

application {
  mainClass = "org.gotson.komga.DesktopApplicationKt"
}

// Work around temporary Compose bugs
configurations.all {
  if (isCanBeResolved || isCanBeConsumed) {
    attributes {
      attribute(Attribute.of("ui", String::class.java), "awt")
    }
  }
}
