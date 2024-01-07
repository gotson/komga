
import nu.studer.gradle.jooq.JooqGenerate
import org.apache.tools.ant.taskdefs.condition.Os
import org.flywaydb.gradle.task.FlywayMigrateTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.util.prefixIfNot

plugins {
  run {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
  }
  id("org.springframework.boot") version "3.1.2"
  id("com.gorylenko.gradle-git-properties") version "2.4.1"
  id("nu.studer.jooq") version "8.2.1"
  id("org.flywaydb.flyway") version "9.7.0"
  id("com.github.johnrengelman.processes") version "0.5.0"
  id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
  id("com.google.devtools.ksp") version "1.8.22-1.0.11"

  jacoco
}

kotlin {
  jvmToolchain(17)
}

val benchmarkSourceSet = sourceSets.create("benchmark") {
  java {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
  }
}

val benchmarkImplementation by configurations.getting {
  extendsFrom(configurations.testImplementation.get())
}
val kaptBenchmark by configurations.getting {
  extendsFrom(configurations.kaptTest.get())
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("reflect"))

  api(platform("org.springframework.boot:spring-boot-dependencies:3.1.2"))

  api("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  implementation("org.springframework.session:spring-session-core")
  implementation("com.github.gotson:spring-session-caffeine:2.0.0")
  implementation("org.springframework.data:spring-data-commons")

  kapt("org.springframework.boot:spring-boot-configuration-processor:3.1.2")

  implementation("org.flywaydb:flyway-core")

  api("io.github.microutils:kotlin-logging-jvm:3.0.5")
  implementation("io.hawt:hawtio-springboot:2.17.6")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

  implementation("commons-io:commons-io:2.15.0")
  implementation("org.apache.commons:commons-lang3:3.13.0")
  implementation("commons-validator:commons-validator:1.7")

  run {
    val luceneVersion = "9.8.0"
    implementation("org.apache.lucene:lucene-core:$luceneVersion")
    implementation("org.apache.lucene:lucene-analysis-common:$luceneVersion")
    implementation("org.apache.lucene:lucene-queryparser:$luceneVersion")
    implementation("org.apache.lucene:lucene-backward-codecs:$luceneVersion")
  }

  implementation("com.ibm.icu:icu4j:74.1")

  implementation("com.appmattus.crypto:cryptohash:0.10.1")

  implementation("org.apache.tika:tika-core:2.9.1")
  implementation("org.apache.commons:commons-compress:1.24.0")
  implementation("com.github.junrar:junrar:7.5.5")
  implementation("com.github.gotson.nightcompress:nightcompress:0.2.0")
  implementation("org.apache.pdfbox:pdfbox:2.0.28")
  implementation("net.grey-panther:natural-comparator:1.1")
  implementation("org.jsoup:jsoup:1.16.2")

  implementation("net.coobird:thumbnailator:0.4.20")
  runtimeOnly("com.twelvemonkeys.imageio:imageio-jpeg:3.10.0")
  runtimeOnly("com.twelvemonkeys.imageio:imageio-tiff:3.10.0")
  runtimeOnly("com.twelvemonkeys.imageio:imageio-webp:3.10.0")
  runtimeOnly("com.github.gotson.nightmonkeys:imageio-jxl:0.6.2")
  runtimeOnly("com.github.gotson.nightmonkeys:imageio-heif:0.6.2")
  runtimeOnly("com.github.gotson.nightmonkeys:imageio-webp:0.6.2")
  // support for jpeg2000
  runtimeOnly("com.github.jai-imageio:jai-imageio-jpeg2000:1.4.0")
  runtimeOnly("org.apache.pdfbox:jbig2-imageio:3.0.4")

  // barcode scanning
  implementation("com.google.zxing:core:3.5.2")

  implementation("com.jakewharton.byteunits:byteunits:0.9.1")

  implementation("com.github.f4b6a3:tsid-creator:5.2.5")

  implementation("com.github.ben-manes.caffeine:caffeine")

  implementation("org.xerial:sqlite-jdbc:3.43.2.2")
  jooqGenerator("org.xerial:sqlite-jdbc:3.43.2.2")

  if (version.toString().endsWith(".0.0")) {
    ksp("com.github.gotson.bestbefore:bestbefore-processor-kotlin:0.1.0")
  }

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("io.mockk:mockk:1.13.5")
  testImplementation("com.google.jimfs:jimfs:1.3.0")

  testImplementation("com.tngtech.archunit:archunit-junit5:1.1.0")

  benchmarkImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  benchmarkImplementation("org.openjdk.jmh:jmh-core:1.37")
  kaptBenchmark("org.openjdk.jmh:jmh-generator-annprocess:1.37")
  kaptBenchmark("org.springframework.boot:spring-boot-configuration-processor:3.1.2")

  developmentOnly("org.springframework.boot:spring-boot-devtools:3.1.2")
}

val webui = "$rootDir/komga-webui"
tasks {
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
      freeCompilerArgs = listOf(
        "-Xjsr305=strict",
        "-opt-in=kotlin.time.ExperimentalTime",
      )
    }
  }

  withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
    maxHeapSize = "1G"
  }

  getByName<Jar>("jar") {
    enabled = true
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
      "install",
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
      "build",
    )
  }

  // copy the webui build into public
  register<Sync>("copyWebDist") {
    group = "web"
    dependsOn("npmBuild")
    from("$webui/dist/")
    into("$projectDir/src/main/resources/public/")
  }

  // modifies index.html to inject ThymeLeaf th: tags
  register<Copy>("prepareThymeLeaf") {
    group = "web"
    dependsOn("copyWebDist")
    from("$webui/dist/index.html")
    into("$projectDir/src/main/resources/public/")
    filter { line ->
      line.replace("((?:src|content|href)=\")([\\w]*/.*?)(\")".toRegex()) {
        it.groups[0]?.value + " th:" + it.groups[1]?.value + "@{" + it.groups[2]?.value?.prefixIfNot("/") + "}" + it.groups[3]?.value
      }
    }
  }

  withType<ProcessResources> {
    filesMatching("application*.yml") {
      expand(project.properties)
    }
    mustRunAfter(getByName("prepareThymeLeaf"))
  }

  register<Test>("benchmark") {
    group = "benchmark"
    inputs.files(benchmarkSourceSet.output)
    testClassesDirs = benchmarkSourceSet.output.classesDirs
    classpath = benchmarkSourceSet.runtimeClasspath
  }
}

springBoot {
  buildInfo {
    // prevent task bootBuildInfo to rerun every time
    excludes = setOf("time")
    properties {
      // but rerun if the gradle.properties file changed
      inputs.file("$rootDir/gradle.properties")
    }
  }
}

val sqliteUrls = mapOf(
  "main" to "jdbc:sqlite:${project.layout.buildDirectory.get()}/generated/flyway/main/database.sqlite",
  "tasks" to "jdbc:sqlite:${project.layout.buildDirectory.get()}/generated/flyway/tasks/tasks.sqlite",
)
val sqliteMigrationDirs = mapOf(
  "main" to listOf(
    "$projectDir/src/flyway/resources/db/migration/sqlite",
    "$projectDir/src/flyway/kotlin/db/migration/sqlite",
  ),
  "tasks" to listOf(
    "$projectDir/src/flyway/resources/tasks/migration/sqlite",
//    "$projectDir/src/flyway/kotlin/tasks/migration/sqlite",
  ),
)

task("flywayMigrateMain", FlywayMigrateTask::class) {
  val id = "main"
  url = sqliteUrls[id]
  locations = arrayOf("classpath:db/migration/sqlite")
  placeholders = mapOf(
    "library-file-hashing" to "true",
    "library-scan-startup" to "false",
    "delete-empty-collections" to "true",
    "delete-empty-read-lists" to "true",
  )
  // in order to include the Java migrations, flywayClasses must be run before flywayMigrate
  dependsOn("flywayClasses")
  sqliteMigrationDirs[id]?.forEach { inputs.dir(it) }
  outputs.dir("${project.layout.buildDirectory.get()}/generated/flyway/$id")
  doFirst {
    delete(outputs.files)
    mkdir("${project.layout.buildDirectory.get()}/generated/flyway/$id")
  }
  mixed = true
}

task("flywayMigrateTasks", FlywayMigrateTask::class) {
  val id = "tasks"
  url = sqliteUrls[id]
  locations = arrayOf("classpath:tasks/migration/sqlite")
  // in order to include the Java migrations, flywayClasses must be run before flywayMigrate
  dependsOn("flywayClasses")
  sqliteMigrationDirs[id]?.forEach { inputs.dir(it) }
  outputs.dir("${project.layout.buildDirectory.get()}/generated/flyway/$id")
  doFirst {
    delete(outputs.files)
    mkdir("${project.layout.buildDirectory.get()}/generated/flyway/$id")
  }
  mixed = true
}

jooq {
  version = "3.18.4"
  configurations {
    create("main") {
      jooqConfiguration.apply {
        logging = org.jooq.meta.jaxb.Logging.WARN
        jdbc.apply {
          driver = "org.sqlite.JDBC"
          url = sqliteUrls["main"]
        }
        generator.apply {
          database.apply {
            name = "org.jooq.meta.sqlite.SQLiteDatabase"
          }
          target.apply {
            packageName = "org.gotson.komga.jooq.main"
          }
        }
      }
    }
    create("tasks") {
      jooqConfiguration.apply {
        logging = org.jooq.meta.jaxb.Logging.WARN
        jdbc.apply {
          driver = "org.sqlite.JDBC"
          url = sqliteUrls["tasks"]
        }
        generator.apply {
          database.apply {
            name = "org.jooq.meta.sqlite.SQLiteDatabase"
          }
          target.apply {
            packageName = "org.gotson.komga.jooq.tasks"
          }
        }
      }
    }
  }
}
tasks.named<JooqGenerate>("generateJooq") {
  sqliteMigrationDirs["main"]?.forEach { inputs.dir(it) }
  allInputsDeclared = true
  dependsOn("flywayMigrateMain")
}
tasks.named<JooqGenerate>("generateTasksJooq") {
  sqliteMigrationDirs["tasks"]?.forEach { inputs.dir(it) }
  allInputsDeclared = true
  dependsOn("flywayMigrateTasks")
}

sourceSets {
  // add a flyway sourceSet
  val flyway by creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
  }
  // main sourceSet depends on the output of flyway sourceSet, and generated jooq classes
  main {
    java {
      output.dir(flyway.output)
      srcDir("build/generated-src/jooq/tasks")
    }
  }
}
tasks.runKtlintCheckOverMainSourceSet {
  dependsOn("generateTasksJooq")
}
tasks.compileKotlin {
  dependsOn("generateTasksJooq")
}

openApi {
  outputDir = file("$projectDir/docs")
  customBootRun {
    args.add("--spring.profiles.active=claim")
    args.add("--server.port=8080")
  }
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
  filter {
    exclude("**/db/migration/**")
  }
}

project.afterEvaluate {
  tasks.named("forkedSpringBootRun") {
    mustRunAfter(tasks.bootJar)
    mustRunAfter(tasks.jar)
  }
}
