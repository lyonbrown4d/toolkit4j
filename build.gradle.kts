import com.vanniktech.maven.publish.MavenPublishBaseExtension
import me.champeau.jmh.JMHPlugin
import name.remal.gradle_plugins.lombok.LombokPlugin
import org.gradle.api.plugins.JavaPlatformPlugin
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
  alias(libs.plugins.dotenv)
  idea
  `java-library`
  alias(libs.plugins.jmh)
  alias(libs.plugins.version.check)
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
  alias(libs.plugins.git)
  alias(libs.plugins.lombok)
  alias(libs.plugins.plantuml)
  alias(libs.plugins.maven.publish)
}

/** Maven coordinates; Central GitHub namespace is typically io.github.{lowercase login}. */
group = "io.github.lyonbrown4d"

version = "0.0.6"

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}

val rootCatalog = rootProject.libs

subprojects {
  if (isPublishableLeafModule()) {
    applyPublishingPropsFromDotenv()
    apply<com.vanniktech.maven.publish.MavenPublishPlugin>()
    if (isBomModule()) {
      apply<JavaPlatformPlugin>()
    } else {
      apply<JMHPlugin>()
      apply<LombokPlugin>()
      apply<JavaLibraryPlugin>()
    }
    group = rootProject.group
    version = rootProject.version

    extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
      publishToMavenCentral()
      signAllPublications()
      pom {
        name.set(stringPropertyOrDefault("POM_NAME") { "toolkit4j-${project.name}" })
        description.set(
          stringPropertyOrDefault("POM_DESCRIPTION") {
            "Lightweight JVM utility toolkit — module \"${project.name}\". See https://github.com/lyonbrown4d/toolkit4j"
          }
        )
        inceptionYear.set(stringPropertyOrDefault("POM_INCEPTION_YEAR") { "2026" })
        url.set(stringPropertyOrDefault("POM_URL") { "https://github.com/lyonbrown4d/toolkit4j" })
        licenses {
          license {
            name.set(
              stringPropertyOrDefault("POM_LICENSE_NAME") { "The Apache License, Version 2.0" }
            )
            url.set(
              stringPropertyOrDefault("POM_LICENSE_URL") {
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
              }
            )
            distribution.set(stringPropertyOrDefault("POM_LICENSE_DIST") { "repo" })
          }
        }
        developers {
          developer {
            id.set(stringPropertyOrDefault("POM_DEVELOPER_ID") { "lyonbrown4d" })
            name.set(stringPropertyOrDefault("POM_DEVELOPER_NAME") { "lyonbrown4d" })
            url.set(stringPropertyOrDefault("POM_DEVELOPER_URL") { "https://github.com/lyonbrown4d" })
          }
        }
        scm {
          url.set(
            stringPropertyOrDefault("POM_SCM_URL") { "https://github.com/lyonbrown4d/toolkit4j" }
          )
          connection.set(
            stringPropertyOrDefault("POM_SCM_CONNECTION") {
              "scm:git:https://github.com/lyonbrown4d/toolkit4j.git"
            }
          )
          developerConnection.set(
            stringPropertyOrDefault("POM_SCM_DEV_CONNECTION") {
              "scm:git:ssh://git@github.com/lyonbrown4d/toolkit4j.git"
            }
          )
        }
      }
    }
    //    tasks.register<Jar>("dokkaHtmlJar") {
    //      dependsOn(tasks.dokkaHtml)
    //      from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    //      archiveClassifier.set("html-docs")
    //    }
    //
    //    tasks.register<Jar>("dokkaJavadocJar") {
    //      dependsOn(tasks.dokkaJavadoc)
    //      from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    //      archiveClassifier.set("javadoc")
    //    }

    if (!isBomModule()) {
      dependencies {
        compileOnly(rootCatalog.jetbrainsAnnotation)
        compileOnly(rootCatalog.slf4j)
        //
        testImplementation(enforcedPlatform(rootCatalog.junitBom))
        testImplementation(rootCatalog.junitJuiter)
        testImplementation(rootCatalog.junitApi)
        testImplementation(rootCatalog.junitEngine)
        testImplementation(rootCatalog.junitInjectFile)
        testRuntimeOnly(rootCatalog.junit.platform.launcher)
        testImplementation(rootCatalog.mockitoCore)
        testImplementation(rootCatalog.mockitoJunit)
        testImplementation(rootCatalog.dataFaker)
        testImplementation(rootCatalog.slf4j)
        testImplementation(rootCatalog.slf4j.simple)
      }

      java {
        // Vanniktech plugin provides plainJavadocJar; keep only one javadoc artifact to avoid
        // duplicate javadoc.jar(.asc).
        withSourcesJar()
      }

      tasks.jar {
        manifest { attributes("Version" to project.version) }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
      // Gradle 9 strict validation: ensure metadata generation has explicit dependency on javadoc
      // jar.
      tasks
        .matching { it.name == "generateMetadataFileForMavenPublication" }
        .configureEach { dependsOn(tasks.matching { task -> task.name == "plainJavadocJar" }) }

      tasks.test {
        useJUnitPlatform()
        configureToolkitTestRuntime()
      }

      tasks.withType<Javadoc>().configureEach {
        val standardOptions = options as StandardJavadocDocletOptions
        standardOptions.encoding = "UTF-8"
        standardOptions.charSet = "UTF-8"
        standardOptions.docEncoding = "UTF-8"
        // Keep malformed Javadoc visible, but do not fail the build on missing comments in
        // delomboked/generated sources.
        standardOptions.addStringOption("Xdoclint:all,-missing", "-quiet")
      }
    }
  }
}

spotless {
  format("misc") {
    target("*.md", ".gitignore")
    endWithNewline()
  }
  java {
    target("**/*.java")
    // GOOGLE style: 2-space indent (AOSP uses 4). Imports are handled by the formatter.
    googleJavaFormat()
    removeUnusedImports()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    ktfmt().googleStyle()
  }
}
