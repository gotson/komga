import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

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

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
  val unstableKeyword = listOf("ALPHA", "RC").any { version.toUpperCase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return unstableKeyword || !isStable
}

allprojects {
  repositories {
    mavenCentral()
  }
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  apply(plugin = "com.github.ben-manes.versions")

  tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    // disallow release candidates as upgradable versions from stable versions
    rejectVersionIf {
      isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
    gradleReleaseChannel = "current"
    checkConstraints = true
  }

}

tasks.wrapper {
  gradleVersion = "6.8.3"
  distributionType = Wrapper.DistributionType.ALL
}
