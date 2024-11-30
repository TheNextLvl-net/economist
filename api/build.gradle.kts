plugins {
    id("java")
}

group = rootProject.group
version = rootProject.version

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withJavadocJar()
    withSourcesJar()
}

tasks.compileJava {
    options.release.set(21)
}

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.36")

    annotationProcessor("org.projectlombok:lombok:1.18.36")
}
