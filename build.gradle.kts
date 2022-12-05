import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.7.22"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

version = "0.1.0"

repositories {
  mavenCentral()
  maven("https://libraries.minecraft.net")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.7.22")
  implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.7.22")
  implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.7.22")
  implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:1.7.22")
  implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven:1.7.22")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
  compileOnly("com.mojang:brigadier:1.0.18")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "17"
}

tasks.jar {
  manifest {
    attributes(
      "Premain-Class" to "mckts.Agent",
    )
  }
}

tasks.shadowJar {
  archiveVersion.set("")
}
