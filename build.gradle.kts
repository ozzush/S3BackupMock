import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("aws.sdk.kotlin:s3:1.0.0")
    implementation("aws.sdk.kotlin:dynamodb:1.0.0")
    implementation("aws.sdk.kotlin:iam:1.0.0")
    implementation("aws.sdk.kotlin:cloudwatch:1.0.0")
    implementation("aws.sdk.kotlin:cognitoidentityprovider:1.0.0")
    implementation("aws.sdk.kotlin:sns:1.0.0")
    implementation("aws.sdk.kotlin:pinpoint:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("org.example.kotlin.MainKt")
}