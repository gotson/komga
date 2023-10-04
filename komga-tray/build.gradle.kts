plugins {
  run {
    kotlin("jvm")
    kotlin("plugin.spring")
  }
  id("com.gorylenko.gradle-git-properties") version "2.4.1"
  id("org.jetbrains.compose") version "1.4.3"
  id("dev.hydraulic.conveyor") version "1.5"
  id("dev.hydraulic.conveyor") version "1.6"
  application
}

group = "org.gotson"

kotlin {
  jvmToolchain(19) // for NightMonkeys
}

dependencies {
  implementation(project(":komga"))

  implementation(compose.desktop.common)

  linuxAmd64(compose.desktop.linux_x64)
  macAmd64(compose.desktop.macos_x64)
  macAarch64(compose.desktop.macos_arm64)
  windowsAmd64(compose.desktop.windows_x64)
}

application {
  mainClass.set("org.gotson.komga.DesktopApplicationKt")
}

// Work around temporary Compose bugs
configurations.all {
  attributes {
    attribute(Attribute.of("ui", String::class.java), "awt")
  }
}
