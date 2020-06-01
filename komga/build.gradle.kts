import com.rohanprabhu.gradle.plugins.kdjooq.database
import com.rohanprabhu.gradle.plugins.kdjooq.generator
import com.rohanprabhu.gradle.plugins.kdjooq.jdbc
import com.rohanprabhu.gradle.plugins.kdjooq.jooqCodegenConfiguration
import com.rohanprabhu.gradle.plugins.kdjooq.target
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  run {
    val kotlinVersion = "1.3.72"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.springframework.boot") version "2.2.6.RELEASE"
  id("com.github.ben-manes.versions") version "0.28.0"
  id("com.gorylenko.gradle-git-properties") version "2.2.2"
  id("com.rohanprabhu.kotlin-dsl-jooq") version "0.4.5"
  id("org.flywaydb.flyway") version "6.4.0"
  jacoco
}

group = "org.gotson"

val developmentOnly = configurations.create("developmentOnly")
configurations.runtimeClasspath.get().extendsFrom(developmentOnly)

repositories {
  jcenter()
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  constraints {
    implementation("org.flywaydb:flyway-core:6.4.0") {
      because("support for H2 1.4.200 requires 6.1.0+")
    }
  }

  implementation(platform("org.springframework.boot:spring-boot-dependencies:2.2.6.RELEASE"))

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-artemis")
  implementation("org.springframework.boot:spring-boot-starter-jooq")

  kapt("org.springframework.boot:spring-boot-configuration-processor:2.2.6.RELEASE")

  implementation("org.apache.activemq:artemis-jms-server")

  implementation("org.flywaydb:flyway-core")

  implementation("io.github.microutils:kotlin-logging:1.7.9")
  implementation("io.micrometer:micrometer-registry-influx")
  implementation("io.hawt:hawtio-springboot:2.10.0")

  run {
    val springdocVersion = "1.3.4"
    implementation("org.springdoc:springdoc-openapi-ui:$springdocVersion")
    implementation("org.springdoc:springdoc-openapi-security:$springdocVersion")
  }

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

  implementation("commons-io:commons-io:2.6")
  implementation("org.apache.commons:commons-lang3:3.10")

  implementation("org.apache.tika:tika-core:1.24.1")
  implementation("org.apache.commons:commons-compress:1.20")
  implementation("com.github.junrar:junrar:4.0.0")
  implementation("org.apache.pdfbox:pdfbox:2.0.19")
  implementation("net.grey-panther:natural-comparator:1.1")
  implementation("org.jsoup:jsoup:1.13.1")

  implementation("net.coobird:thumbnailator:0.4.11")
  implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.5")
  implementation("com.twelvemonkeys.imageio:imageio-tiff:3.5")
  implementation(files("$projectDir/libs/webp-imageio-decoder-plugin-0.2.jar"))
  // support for jpeg2000
  implementation("com.github.jai-imageio:jai-imageio-jpeg2000:1.3.0")
  implementation("org.apache.pdfbox:jbig2-imageio:3.0.3")

  implementation("com.jakewharton.byteunits:byteunits:0.9.1")

  runtimeOnly("com.h2database:h2:1.4.200")
  jooqGeneratorRuntime("com.h2database:h2:1.4.200")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.ninja-squad:springmockk:2.0.1")
  testImplementation("io.mockk:mockk:1.10.0")
  testImplementation("com.google.jimfs:jimfs:1.1")

  testImplementation("com.tngtech.archunit:archunit-junit5:0.13.1")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
}

val webui = "$rootDir/komga-webui"
tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.time.ExperimentalTime")
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

  //unpack Spring Boot's fat jar for better Docker image layering
  register<Sync>("unpack") {
    dependsOn(bootJar)
    from(zipTree(getByName("bootJar").outputs.files.singleFile))
    into("$buildDir/dependency")
  }

  register<Exec>("npmInstall") {
    group = "web"
    workingDir(webui)
    inputs.file("$webui/package.json")
    outputs.dir("$webui/node_modules")
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
    workingDir(webui)
    inputs.dir(webui)
    outputs.dir("$webui/dist")
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

  //copy the webui build into public
  register<Sync>("copyWebDist") {
    group = "web"
    dependsOn("npmBuild")
    from("$webui/dist/")
    into("$projectDir/src/main/resources/public/")
  }
}

springBoot {
  buildInfo {
    properties {
      // prevent task bootBuildInfo to rerun every time
      time = null
    }
  }
}

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.MappedSuperclass")
  annotation("javax.persistence.Embeddable")
}

sourceSets {
  //add a flyway sourceSet
  val flyway by creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
  }
  //main sourceSet depends on the output of flyway sourceSet
  main {
    output.dir(flyway.output)
  }
}

val jooqDb = mapOf(
  "url" to "jdbc:h2:${project.buildDir}/generated/flyway/h2",
  "schema" to "PUBLIC",
  "user" to "sa",
  "password" to ""
)
val migrationDirs = listOf(
  "$projectDir/src/flyway/resources/db/migration",
  "$projectDir/src/flyway/kotlin/db/migration"
)
flyway {
  url = jooqDb["url"]
  user = jooqDb["user"]
  password = jooqDb["password"]
  schemas = arrayOf(jooqDb["schema"])
  locations = arrayOf("classpath:db/migration")
}
//in order to include the Java migrations, flywayClasses must be run before flywayMigrate
tasks.flywayMigrate {
  dependsOn("flywayClasses")
  migrationDirs.forEach { inputs.dir(it) }
  outputs.dir("${project.buildDir}/generated/flyway")
  doFirst { delete(outputs.files) }
}

jooqGenerator {
  jooqVersion = "3.13.1"
  configuration("primary", project.sourceSets.getByName("main")) {
    databaseSources = migrationDirs

    configuration = jooqCodegenConfiguration {
      jdbc {
        username = jooqDb["user"]
        password = jooqDb["password"]
        driver = "org.h2.Driver"
        url = jooqDb["url"]
      }

      generator {
        target {
          packageName = "org.gotson.komga.jooq"
          directory = "${project.buildDir}/generated/jooq/primary"
        }

        database {
          name = "org.jooq.meta.h2.H2Database"
          inputSchema = jooqDb["schema"]
        }
      }
    }
  }
}
val `jooq-codegen-primary` by project.tasks
`jooq-codegen-primary`.dependsOn("flywayMigrate")
