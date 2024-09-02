import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    id("java")

    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.modrinth.minotaur") version "2.+"
}

group = "net.thenextlvl.economist"
version = "1.0.0"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
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
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("net.thenextlvl.core:annotations:2.0.1")
    compileOnly("net.thenextlvl.services:service-io:1.0.0")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.projectlombok:lombok:1.18.34")

    implementation("org.bstats:bstats-bukkit:3.0.3")
    implementation(project(":api"))

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.shadowJar {
    relocate("org.bstats", "${rootProject.group}.metrics")
}

tasks.test {
    useJUnitPlatform()
}

paper {
    name = "Economist"
    main = "net.thenextlvl.economist.EconomistPlugin"
    author = "NonSwag"
    apiVersion = "1.21"
    foliaSupported = true

    website = "https://thenextlvl.net"
}

val versionString: String = project.version as String
val isRelease: Boolean = !versionString.contains("-pre")

val versions: List<String> = (property("gameVersions") as String)
    .split(",")
    .map { it.trim() }

hangarPublish { // docs - https://docs.papermc.io/misc/hangar-publishing
    publications.register("plugin") {
        id.set("Economist")
        version.set(versionString)
        channel.set(if (isRelease) "Release" else "Snapshot")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms.register(Platforms.PAPER) {
            jar.set(tasks.shadowJar.flatMap { it.archiveFile })
            platformVersions.set(versions)
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("") // todo: create project
    versionType = if (isRelease) "release" else "beta"
    uploadFile.set(tasks.shadowJar)
    gameVersions.set(versions)
    loaders.add("paper")
    loaders.add("folia")
}