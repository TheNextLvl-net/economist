import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")

    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
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
    maven("https://jitpack.io")
    maven("https://repo.thenextlvl.net/releases")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("net.thenextlvl.core:annotations:2.0.1")
    compileOnly("net.thenextlvl.services:service-io:1.0.2")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.projectlombok:lombok:1.18.34")

    implementation("net.thenextlvl.core:files:1.0.5")
    implementation("net.thenextlvl.core:i18n:1.0.19")
    implementation("net.thenextlvl.core:paper:1.4.1")
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

    serverDependencies {
        register("ServiceIO") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }

    permissions {
        register("economist.admin") {
            default = BukkitPluginDescription.Permission.Default.OP
            children = listOf(
                "economist.balance-top.world",
                "economist.balance.world",
                "economist.bank",
                "economist.pay.world",
            )
        }

        register("economist.balance") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("economist.balance.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            children = listOf("economist.balance")
        }
        register("economist.balance.world") {
            default = BukkitPluginDescription.Permission.Default.OP
            children = listOf("economist.balance.others")
        }

        register("economist.balance-top") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("economist.balance-top.world") {
            default = BukkitPluginDescription.Permission.Default.OP
            children = listOf("economist.balance-top")
        }

        register("economist.bank") {
            default = BukkitPluginDescription.Permission.Default.OP
        }

        register("economist.pay") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("economist.pay.world") {
            default = BukkitPluginDescription.Permission.Default.OP
            children = listOf("economist.pay")
        }
    }
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