plugins {
  run {
    val kotlinVersion = "1.4.31"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
  id("com.github.ben-manes.versions") version "0.38.0"
}

allprojects {
  repositories {
    mavenCentral()
  }
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  apply(plugin = "com.github.ben-manes.versions")
}

tasks.wrapper {
  gradleVersion = "6.8.3"
  distributionType = Wrapper.DistributionType.ALL
}
