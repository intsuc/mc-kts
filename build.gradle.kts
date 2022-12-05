import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.22"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.7.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.7.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.7.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:1.7.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven:1.7.22")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
