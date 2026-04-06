plugins {
    java
    application
    id("io.freefair.lombok") version "9.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.21.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.21")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.0-M1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:6.1.0-M1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.1.0-M1")
}

group = "org.gamegirl.emu"
version = "0.1-ALPHA"
description = "GameGirl Emulator"
java.sourceCompatibility = JavaVersion.VERSION_21

application {
    mainClass = "Main"
}

tasks.test {
    useJUnitPlatform()
}