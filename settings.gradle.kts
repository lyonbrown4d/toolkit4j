pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
  id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.2"
  id("com.gradle.enterprise") version "3.13.4"
}

buildCache {
  local {
    isEnabled = true
    directory = File(rootProject.projectDir, ".gradle/build-cache")
  }
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

gitHooks {
  //    preCommit {
  //        logger.log(LogLevel.INFO,"pre commit")
  //    }
  //    createHooks()
}

rootProject.name = "toolkit4j"

include("libs:collection")

include("libs:text")

include("libs:net")

include("libs:data-model")

include("libs:hibernate-snowflake-id")

include("libs:quartz-task")

include("libs:toolkit4j-bom")

project(":libs:toolkit4j-bom").projectDir = file("libs/bom")
