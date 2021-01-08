plugins {
  id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

allprojects {
  repositories {
    jcenter()
    mavenCentral()
  }
}

tasks.wrapper {
  gradleVersion = "6.7.1"
  distributionType = Wrapper.DistributionType.ALL
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
  version.set("0.40.0")
  filter {
    exclude("**/db/migration/**")
  }
}
