pluginManagement {
  val quarkusPluginVersion: String by settings
  val quarkusPluginId: String by settings
  repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
    maven {
      url = uri("https://raw.githubusercontent.com/graalvm/native-build-tools/snapshots")
    }
  }
  plugins {
    id(quarkusPluginId) version quarkusPluginVersion
    id("org.graalvm.buildtools.native") version "0.10.6"
    kotlin("jvm") version "2.1.10"
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "multi-comfyui-gateway"
