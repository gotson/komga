import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  run {
    kotlin("jvm")
    kotlin("plugin.spring")
  }
  id("com.gorylenko.gradle-git-properties") version "2.4.1"
  id("org.jetbrains.compose") version "1.5.11"
  id("dev.hydraulic.conveyor") version "1.8"
  application
}

group = "org.gotson"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}

dependencies {
  implementation(project(":komga"))

  implementation(compose.desktop.currentOs)

  linuxAmd64(compose.desktop.linux_x64)
  macAmd64(compose.desktop.macos_x64)
  macAarch64(compose.desktop.macos_arm64)
  windowsAmd64(compose.desktop.windows_x64)
}

application {
  mainClass = "org.gotson.komga.DesktopApplicationKt"
}

// Work around temporary Compose bugs
configurations.all {
  attributes {
    attribute(Attribute.of("ui", String::class.java), "awt")
  }
}
