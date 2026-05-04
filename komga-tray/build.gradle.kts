import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  run {
    kotlin("jvm")
    kotlin("plugin.spring")
  }
  alias(libs.plugins.gradleGitProperties)
  id("org.jetbrains.compose") version "1.8.2"
  id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
  id("dev.hydraulic.conveyor") version "1.13"
  application
}

group = "org.gotson"

repositories {
  mavenCentral()
  google()
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    jvmTarget = JvmTarget.JVM_25
  }
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "25"
    targetCompatibility = "25"
  }
}

dependencies {
  implementation(project(":komga"))

  implementation(compose.desktop.currentOs)
  implementation(compose.components.resources)

  linuxAmd64(compose.desktop.linux_x64)
  macAmd64(compose.desktop.macos_x64)
  macAarch64(compose.desktop.macos_arm64)
  windowsAmd64(compose.desktop.windows_x64)
}

application {
  mainClass = "org.gotson.komga.DesktopApplicationKt"
}

// Work around temporary Compose bugs
configurations.configureEach {
  if (name != "archives" && (isCanBeConsumed || isCanBeResolved)) {
    attributes {
      attribute(Attribute.of("ui", String::class.java), "awt")
    }
  }
}
