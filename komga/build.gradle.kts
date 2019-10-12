import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import com.palantir.gradle.docker.DockerExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  run {
    val kotlinVersion = "1.3.50"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.springframework.boot") version "2.1.9.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  id("com.github.ben-manes.versions") version "0.25.0"
  id("com.palantir.docker") version "0.22.1"
  id("com.github.breadmoirai.github-release") version "2.2.9"
  jacoco
}

group = "org.gotson"
version = "0.6.1"

val developmentOnly = configurations.create("developmentOnly")
configurations.runtimeClasspath.get().extendsFrom(developmentOnly)

repositories {
  jcenter()
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")

  kapt("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.flywaydb:flyway-core")

//  implementation("com.github.ben-manes.caffeine:caffeine:2.7.0")

  implementation("io.github.microutils:kotlin-logging:1.7.6")

  run {
    val springfoxVersion = "2.9.2"
    implementation("io.springfox:springfox-swagger2:$springfoxVersion")
    implementation("io.springfox:springfox-swagger-ui:$springfoxVersion")
  }

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

  implementation("com.github.klinq:klinq-jpaspec:0.8")

  implementation("commons-io:commons-io:2.6")
  implementation("org.apache.commons:commons-lang3:3.9")

  implementation("org.apache.tika:tika-core:1.22")
  implementation("com.github.junrar:junrar:4.0.0")
  implementation("org.apache.pdfbox:pdfbox:2.0.17")
  implementation("net.grey-panther:natural-comparator:1.1")

  implementation("net.coobird:thumbnailator:0.4.8")
  implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.4.2")
  implementation("com.twelvemonkeys.imageio:imageio-tiff:3.4.2")
  implementation(files("$projectDir/libs/webp-imageio-decoder-plugin-0.2.jar"))

  runtimeOnly("com.h2database:h2")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "junit")
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.junit.jupiter:junit-jupiter-engine")
  testImplementation("com.ninja-squad:springmockk:1.1.3")
  testImplementation("io.mockk:mockk:1.9.3")
  testImplementation("com.google.jimfs:jimfs:1.1")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
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

  register<Delete>("deletePublic") {
    delete("$projectDir/src/main/resources/public/")
  }

  register<Exec>("npmInstall") {
    workingDir("$rootDir/komga-webui")
    commandLine(
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
          "npm.cmd"
        } else {
          "npm"
        },
        "install"
    )
  }

  register<Exec>("npmBuild") {
    dependsOn("npmInstall")
    workingDir("$rootDir/komga-webui")
    commandLine(
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
          "npm.cmd"
        } else {
          "npm"
        },
        "run",
        "build"
    )
  }

  register<Copy>("copyWebDist") {
    dependsOn("deletePublic", "npmBuild")
    from("$rootDir/komga-webui/dist/")
    into("$projectDir/src/main/resources/public/")
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
  token(findProperty("github.token")?.toString() ?: System.getenv("GITHUB_TOKEN"))
  owner("gotson")
  repo("komga")
  releaseAssets(tasks.getByName("bootJar").outputs.files.singleFile)
}
tasks.withType<GithubReleaseTask> {
  dependsOn(tasks.bootJar)
}
