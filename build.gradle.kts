import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java
  id("io.quarkus")
  id("org.graalvm.buildtools.native") version "0.10.6"
  kotlin("jvm")
  kotlin("plugin.allopen") version "2.1.10"
  kotlin("plugin.noarg") version "2.1.10"
}

repositories {
  mavenCentral()
  mavenLocal()
}

//val isLocal: String by project
val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {

  implementation(kotlin("stdlib-jdk8"))
  implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
  implementation(enforcedPlatform("${quarkusPlatformGroupId}:quarkus-google-cloud-services-bom:${quarkusPlatformVersion}"))
  implementation(platform("com.google.cloud:libraries-bom:26.51.0"))
  implementation("io.quarkus:quarkus-arc:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-rest:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-rest-qute:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-smallrye-jwt:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-rest-jackson:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-websockets-next:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-smallrye-openapi:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-smallrye-jwt-build:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-rest-client-jackson:$quarkusPlatformVersion")
  implementation("io.quarkus:quarkus-websockets-next-kotlin:$quarkusPlatformVersion")
  implementation("org.apache.httpcomponents.core5:httpcore5:5.3.3")

  // google
  implementation("com.google.code.gson:gson:2.10.1")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

  testImplementation("io.quarkus:quarkus-junit5:$quarkusPlatformVersion")
  testImplementation("io.rest-assured:rest-assured:5.5.1")

}

group = "ai.bitflow.comfyui.multi.gateway"
version = "1.0.0-SNAPSHOT"

java {
}

allOpen {
  annotation("jakarta.ws.rs.Path")
  annotation("jakarta.persistence.Entity")
  annotation("io.quarkus.test.junit.QuarkusTest")
  annotation("jakarta.enterprise.context.ApplicationScoped")
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
  options.compilerArgs.add("-parameters")
}

tasks.withType<KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
    javaParameters.set(true)
  }
}

tasks.withType<Test> {
  systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

kotlin {
  jvmToolchain(21)
}
