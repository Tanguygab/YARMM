plugins {
    kotlin("jvm") version libs.versions.kotlin
}

group = "io.tanguygab"
version = "1.0.1"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.conditionalactions)
    compileOnly(files("../../dependencies/TAB.jar"))
}

kotlin {
    jvmToolchain(21)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    val props = mapOf("version" to version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
