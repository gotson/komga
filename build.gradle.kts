import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
  run {
    val kotlinVersion = "1.7.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
  id("com.github.ben-manes.versions") version "0.44.0"
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

  configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("0.45.2") // plugin does not support 0.46+ - see https://github.com/JLLeitschuh/ktlint-gradle/issues/589
    filter {
      exclude("**/db/migration/**")
    }
  }
}

tasks.wrapper {
  gradleVersion = "7.5.1"
  distributionType = Wrapper.DistributionType.ALL
}
