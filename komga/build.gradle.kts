import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.crypto.checksum.Checksum
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  run {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
  }
  id("org.springframework.boot") version "2.5.8"
  id("com.gorylenko.gradle-git-properties") version "2.3.2"
  id("nu.studer.jooq") version "5.2.2" // 6.0.0 requires Java 11
  id("org.flywaydb.flyway") version "7.15.0"
  id("com.github.johnrengelman.processes") version "0.5.0"
  id("org.springdoc.openapi-gradle-plugin") version "1.3.3"
  id("org.gradle.crypto.checksum") version "1.4.0"

  jacoco
}

group = "org.gotson"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  // 2.6.0 brings assertj-core 3.21.0 which causes type inference issues in Kotlin, should be fixed in Kotlin 1.6.20
  implementation(platform("org.springframework.boot:spring-boot-dependencies:2.5.8"))

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-artemis")
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  implementation("org.springframework.session:spring-session-core")
  implementation("com.github.gotson:spring-session-caffeine:1.0.3")

  kapt("org.springframework.boot:spring-boot-configuration-processor:2.5.8")

  implementation("org.apache.activemq:artemis-jms-server")

  implementation("org.flywaydb:flyway-core")

  implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
  implementation("io.micrometer:micrometer-registry-influx")
  implementation("io.hawt:hawtio-springboot:2.13.5")

  run {
    val springdocVersion = "1.5.12" // later uses Spring Boot 2.6.0
    implementation("org.springdoc:springdoc-openapi-ui:$springdocVersion")
    implementation("org.springdoc:springdoc-openapi-security:$springdocVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$springdocVersion")
    implementation("org.springdoc:springdoc-openapi-data-rest:$springdocVersion")
  }

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

  implementation("commons-io:commons-io:2.11.0")
  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("commons-validator:commons-validator:1.7")

  run {
    val luceneVersion = "8.11.1" // 9.0.0 requires Java 11
    implementation("org.apache.lucene:lucene-core:$luceneVersion")
    implementation("org.apache.lucene:lucene-analyzers-common:$luceneVersion")
    implementation("org.apache.lucene:lucene-queryparser:$luceneVersion")
  }

  implementation("com.ibm.icu:icu4j:70.1")

  implementation("org.apache.tika:tika-core:2.2.0")
  implementation("org.apache.commons:commons-compress:1.21")
  implementation("com.github.junrar:junrar:7.4.0")
  implementation("org.apache.pdfbox:pdfbox:2.0.25")
  implementation("net.grey-panther:natural-comparator:1.1")
  implementation("org.jsoup:jsoup:1.14.3")

  implementation("net.coobird:thumbnailator:0.4.15")
  runtimeOnly("com.twelvemonkeys.imageio:imageio-jpeg:3.8.0")
  runtimeOnly("com.twelvemonkeys.imageio:imageio-tiff:3.8.0")
  runtimeOnly("com.twelvemonkeys.imageio:imageio-webp:3.8.0")
  implementation("com.github.gotson:webp-imageio:0.2.2")
  // support for jpeg2000
  runtimeOnly("com.github.jai-imageio:jai-imageio-jpeg2000:1.4.0")
  runtimeOnly("org.apache.pdfbox:jbig2-imageio:3.0.3")

  // barcode scanning
  implementation("com.google.zxing:core:3.4.1")

  implementation("com.jakewharton.byteunits:byteunits:0.9.1")

  implementation("com.github.f4b6a3:tsid-creator:4.1.4")

  implementation("com.github.ben-manes.caffeine:caffeine:2.9.3") // 3.0.0 requires Java 11

  implementation("org.xerial:sqlite-jdbc:3.36.0.3")
  jooqGenerator("org.xerial:sqlite-jdbc:3.36.0.3")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.ninja-squad:springmockk:3.1.0")
  testImplementation("io.mockk:mockk:1.12.1")
  testImplementation("com.google.jimfs:jimfs:1.2")

  testImplementation("com.tngtech.archunit:archunit-junit5:0.22.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools:2.5.8")
}

val webui = "$rootDir/komga-webui"
tasks {
  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs = listOf(
        "-Xjsr305=strict",
        "-Xopt-in=kotlin.time.ExperimentalTime",
      )
    }
  }

  withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
    maxHeapSize = "1G"
  }

  withType<ProcessResources> {
    filesMatching("application*.yml") {
      expand(project.properties)
    }
  }

  getByName<Jar>("jar") {
    enabled = false
  }

  register<Checksum>("checksums") {
    group = "build"
    inputFiles.from(files(bootJar))
    appendFileNameToChecksum.set(true)
  }

  // unpack Spring Boot's fat jar for better Docker image layering
  register<JavaExec>("unpack") {
    dependsOn(bootJar)
    classpath = files(bootJar)
    jvmArgs = listOf("-Djarmode=layertools")
    args = "extract --destination $buildDir/dependency".split(" ")
    doFirst {
      delete("$buildDir/dependency")
    }
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

  // copy the webui build into public
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
      // but rerun if the gradle.properties file changed
      inputs.file("$rootDir/gradle.properties")
    }
  }
}

sourceSets {
  // add a flyway sourceSet
  val flyway by creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
  }
  // main sourceSet depends on the output of flyway sourceSet
  main {
    output.dir(flyway.output)
  }
}

val dbSqlite = mapOf(
  "url" to "jdbc:sqlite:${project.buildDir}/generated/flyway/database.sqlite"
)
val migrationDirsSqlite = listOf(
  "$projectDir/src/flyway/resources/db/migration/sqlite",
  "$projectDir/src/flyway/kotlin/db/migration/sqlite"
)
flyway {
  url = dbSqlite["url"]
  locations = arrayOf("classpath:db/migration/sqlite")
  placeholders = mapOf("library-file-hashing" to "true")
}
tasks.flywayMigrate {
  // in order to include the Java migrations, flywayClasses must be run before flywayMigrate
  dependsOn("flywayClasses")
  migrationDirsSqlite.forEach { inputs.dir(it) }
  outputs.dir("${project.buildDir}/generated/flyway")
  doFirst {
    delete(outputs.files)
    mkdir("${project.buildDir}/generated/flyway")
  }
  mixed = true
}

jooq {
  version.set("3.14.8")
  configurations {
    create("main") {
      jooqConfiguration.apply {
        logging = org.jooq.meta.jaxb.Logging.WARN
        jdbc.apply {
          driver = "org.sqlite.JDBC"
          url = dbSqlite["url"]
        }
        generator.apply {
          database.apply {
            name = "org.jooq.meta.sqlite.SQLiteDatabase"
          }
          target.apply {
            packageName = "org.gotson.komga.jooq"
          }
        }
      }
    }
  }
}
tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
  migrationDirsSqlite.forEach { inputs.dir(it) }
  allInputsDeclared.set(true)
  dependsOn("flywayMigrate")
}

openApi {
  outputDir.set(file("$projectDir/docs"))
  forkProperties.set("-Dspring.profiles.active=claim")
}
