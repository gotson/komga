plugins {
  run {
    val kotlinVersion = "1.4.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

allprojects {
  repositories {
    mavenCentral()
  }
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks.wrapper {
  gradleVersion = "6.7.1"
  distributionType = Wrapper.DistributionType.ALL
}
