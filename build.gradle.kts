
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jreleaser.model.Active
import org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR
import org.jreleaser.model.api.common.Apply

plugins {
  run {
    val kotlinVersion = "1.8.22"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.jlleitschuh.gradle.ktlint") version "11.4.2"
  id("com.github.ben-manes.versions") version "0.46.0"
  id("org.jreleaser") version "1.7.0"
}

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
  val unstableKeyword = listOf("ALPHA", "RC").any { version.uppercase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return unstableKeyword || !isStable
}

group = "org.gotson"

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
    version.set("0.48.2")
  }
}

tasks.wrapper {
  gradleVersion = "8.1.1"
  distributionType = Wrapper.DistributionType.ALL
}

jreleaser {
  project {
    description.set("Media server for comics/mangas/BDs with API and OPDS support")
    copyright.set("Gauthier Roebroeck")
    authors.add("Gauthier Roebroeck")
    license.set("MIT")
    links {
      homepage.set("https://komga.org")
    }
  }

  release {
    github {
      discussionCategoryName.set("Announcements")

      changelog {
        formatted.set(Active.ALWAYS)
        preset.set("conventional-commits")
        skipMergeCommits.set(true)
        links.set(true)
        format.set("- {{#commitIsConventional}}{{#conventionalCommitIsBreakingChange}}🚨 {{/conventionalCommitIsBreakingChange}}{{#conventionalCommitScope}}**{{conventionalCommitScope}}**: {{/conventionalCommitScope}}{{conventionalCommitDescription}}{{#conventionalCommitBreakingChangeContent}}: *{{conventionalCommitBreakingChangeContent}}*{{/conventionalCommitBreakingChangeContent}} ({{commitShortHash}}){{/commitIsConventional}}{{^commitIsConventional}}{{commitTitle}} ({{commitShortHash}}){{/commitIsConventional}}{{#commitHasIssues}}, closes{{#commitIssues}} {{issue}}{{/commitIssues}}{{/commitHasIssues}}")
        hide {
          uncategorized.set(true)
          contributors.set(listOf("Weblate", "GitHub", "semantic-release-bot", "[bot]", "github-actions"))
        }
        excludeLabels.add("chore")
        category {
          title.set("🏎 Perf")
          key.set("perf")
          labels.add("perf")
          order.set(25)
        }
        category {
          title.set("🌐 Translation")
          key.set("i18n")
          labels.add("i18n")
          order.set(70)
        }
        labeler {
          label.set("perf")
          title.set("regex:^(?:perf(?:\\(.*\\))?!?):\\s.*")
          order.set(120)
        }
        labeler {
          label.set("i18n")
          title.set("regex:^(?:i18n(?:\\(.*\\))?!?):\\s.*")
          order.set(130)
        }
        extraProperties.put("categorizeScopes", true)
        append {
          enabled.set(true)
          title.set("# [{{projectVersion}}]({{repoUrl}}/compare/{{previousTagName}}...{{tagName}}) ({{#f_now}}YYYY-MM-dd{{/f_now}})")
          target.set(rootDir.resolve("CHANGELOG.md"))
          content.set(
            """
            {{changelogTitle}}
            {{changelogChanges}}
            """.trimIndent(),
          )
        }
      }

      issues {
        enabled.set(true)
        comment.set("🎉 This issue has been resolved in `{{tagName}}` ([Release Notes]({{releaseNotesUrl}}))")
        applyMilestone.set(Apply.ALWAYS)
        label {
          name.set("released")
          description.set("Issue has been released")
          color.set("#ededed")
        }
      }
    }
  }

  files {
    active.set(Active.RELEASE)
    // workaround as glob doesn't seem to work https://github.com/jreleaser/jreleaser/issues/1466
    file("./output/release").listFiles()?.forEach {
      artifact {
        path.set(it)
      }
    }
  }

  distributions {
    create("komga") {
      active.set(Active.RELEASE)
      distributionType.set(SINGLE_JAR)
      artifact {
        path.set(rootDir.resolve("komga/build/libs/komga-{{projectVersion}}.jar"))
      }
    }
  }

  packagers {
    docker {
      active.set(Active.RELEASE)
      continueOnError.set(true)
      templateDirectory.set(rootDir.resolve("komga/docker"))
      repository.active.set(Active.NEVER)
      buildArgs.set(listOf("--cache-from", "gotson/komga:latest"))
      imageNames.set(
        listOf(
          "komga:latest",
          "komga:{{projectVersion}}",
          "komga:{{projectVersionMajor}}.x",
        ),
      )
      registries {
        create("docker.io") { externalLogin.set(true) }
        create("ghcr.io") { externalLogin.set(true) }
      }
      buildx {
        enabled.set(true)
        createBuilder.set(false)
        platforms.set(
          listOf(
            "linux/amd64",
            "linux/arm/v7",
            "linux/arm64/v8",
          ),
        )
      }
    }
  }
}
