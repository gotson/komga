import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import com.palantir.gradle.docker.DockerExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  run {
    val kotlinVersion = "1.3.41"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.springframework.boot") version "2.1.6.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  id("com.github.ben-manes.versions") version "0.22.0"
  id("com.palantir.docker") version "0.22.1"
  id("com.github.breadmoirai.github-release") version "2.2.9"
  jacoco
}

group = "org.gotson"
version = "0.2.0"

repositories {
  jcenter()
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M2")

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")

  kapt("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.flywaydb:flyway-core:5.2.4")

//  implementation("com.github.ben-manes.caffeine:caffeine:2.7.0")

  implementation("io.github.microutils:kotlin-logging:1.6.26")

  run {
    val springfoxVersion = "2.9.2"
    implementation("io.springfox:springfox-swagger2:$springfoxVersion")
    implementation("io.springfox:springfox-swagger-ui:$springfoxVersion")
  }

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")

  implementation("com.github.klinq:klinq-jpaspec:0.8")

  implementation("commons-io:commons-io:2.6")

  implementation("org.apache.commons:commons-lang3:3.9")

  implementation("org.apache.tika:tika-core:1.22")
  implementation("com.github.junrar:junrar:4.0.0")
  implementation("org.apache.pdfbox:pdfbox:2.0.16")
  implementation("net.grey-panther:natural-comparator:1.1")

  implementation("net.coobird:thumbnailator:0.4.8")
  implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.4.2")
  implementation("com.twelvemonkeys.imageio:imageio-tiff:3.4.2")

  runtimeOnly("com.h2database:h2:1.4.199")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "junit")
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.junit.jupiter:junit-jupiter:5.5.0")
  testImplementation("com.ninja-squad:springmockk:1.1.2")
  testImplementation("io.mockk:mockk:1.9.3")
  testImplementation("com.google.jimfs:jimfs:1.1")
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs = listOf("-Xjsr305=strict")
    }
  }

  withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
  }

  withType<ProcessResources> {
    filesMatching("application*.yml") {
      expand(project.properties)
    }
  }

  register<Copy>("unpack") {
    dependsOn(bootJar)
    from(zipTree(getByName("bootJar").outputs.files.singleFile))
    into("$buildDir/dependency")
  }
}

configure<DockerExtension> {
  name = "gotson/komga"
  tag("latest", "$name:latest")
  tag("semVer", "$name:$version")
  tag("beta", "$name:beta")
  copySpec.from(tasks.getByName("unpack").outputs).into("dependency")
  buildArgs(mapOf("DEPENDENCY" to "dependency"))
}

githubRelease {
  token(findProperty("github.token")?.toString())
  owner("gotson")
  repo("komga")
  releaseAssets(tasks.getByName("bootJar").outputs.files.singleFile)
}
tasks.withType<GithubReleaseTask> {
  dependsOn(tasks.bootJar)
}

tasks.register("release") {
  description = "Performs a release on Github as well as Docker for tags latest and semVer"
  group = "publishing"
  dependsOn(
      tasks.clean,
      tasks.test,
      tasks.getByName("dockerPushLatest"),
      tasks.getByName("dockerPushSemVer"),
      tasks.githubRelease
  )
  doLast { println("release") }
}