plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.gradle.dokka)
    implementation(libs.gradle.errorprone)
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.maven.publish)
    implementation(libs.gradle.spotless)
}
