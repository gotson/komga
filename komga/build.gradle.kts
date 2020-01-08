import com.palantir.gradle.docker.DockerExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  run {
    val kotlinVersion = "1.3.61"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.springframework.boot") version "2.2.2.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  id("com.github.ben-manes.versions") version "0.27.0"
  id("com.palantir.docker") version "0.22.1"
  id("com.gorylenko.gradle-git-properties") version "2.2.0"
  jacoco
}

group = "org.gotson"

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

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")

  kapt("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.flywaydb:flyway-core")
  implementation("org.hibernate:hibernate-jcache")

  implementation("com.github.ben-manes.caffeine:caffeine")
  implementation("com.github.ben-manes.caffeine:jcache")

  implementation("io.github.microutils:kotlin-logging:1.7.8")
  implementation("io.micrometer:micrometer-registry-influx")

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

  implementation("org.apache.tika:tika-core:1.23")
  implementation("org.apache.commons:commons-compress:1.19")
  implementation("com.github.junrar:junrar:4.0.0")
  implementation("org.apache.pdfbox:pdfbox:2.0.17")
  implementation("net.grey-panther:natural-comparator:1.1")

  implementation("net.coobird:thumbnailator:0.4.8")
  implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.4.2")
  implementation("com.twelvemonkeys.imageio:imageio-tiff:3.4.2")
  implementation(files("$projectDir/libs/webp-imageio-decoder-plugin-0.2.jar"))
  // support for jpeg2000
  implementation("com.github.jai-imageio:jai-imageio-jpeg2000:1.3.0")
  implementation("org.apache.pdfbox:jbig2-imageio:3.0.3")

  implementation("com.jakewharton.byteunits:byteunits:0.9.1")

  runtimeOnly("com.h2database:h2")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.ninja-squad:springmockk:2.0.0")
  testImplementation("io.mockk:mockk:1.9.3")
  testImplementation("com.google.jimfs:jimfs:1.1")

  run {
    val archunitVersion = "0.12.0"
    testImplementation("com.tngtech.archunit:archunit-junit5-api:$archunitVersion")
    testRuntime("com.tngtech.archunit:archunit-junit5-engine:$archunitVersion")
  }

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
    group = "web"
    delete("$projectDir/src/main/resources/public/")
  }

  register<Exec>("npmInstall") {
    group = "web"
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
    group = "web"
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
    group = "web"
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

springBoot {
  buildInfo()
}

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.MappedSuperclass")
  annotation("javax.persistence.Embeddable")
}
