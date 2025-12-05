import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")

    id("com.gradleup.shadow") version "9.3.0"
    id("com.modrinth.minotaur") version "2.+"
    id("de.eldoria.plugin-yml.paper") version "0.8.0"
    id("io.papermc.hangar-publish-plugin") version "0.1.3"
}

group = "net.thenextlvl.economist"
version = "0.2.4"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.compileJava {
    options.release.set(21)
}

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("net.thenextlvl.services:service-io:2.3.1")

    implementation("net.thenextlvl.core:i18n:3.2.2")
    implementation("net.thenextlvl.core:files:3.0.1")
    implementation("net.thenextlvl.core:paper:2.3.1")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation(project(":api"))
}

tasks.shadowJar {
    relocate("org.bstats", "${rootProject.group}.metrics")
}

paper {
    name = "Economist"
    main = "net.thenextlvl.economist.EconomistPlugin"
    author = "NonSwag"
    apiVersion = "1.21"
    foliaSupported = true
    description = "The next generation economy plugin with database and multiserver support"

    website = "https://thenextlvl.net"

    serverDependencies {
        register("ServiceIO") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }

    permissions {
        register("economist.bank.admin") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Admin permission to manage banks."
            children = listOf(
                "economist.bank.create.others",
                "economist.bank.delete.others",
                "economist.bank.manage.others",
            )
        }

        register("economist.account.create") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to create accounts."
            children = listOf("economist.account")
        }
        register("economist.account.create.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to create accounts for others."
            children = listOf("economist.account.create")
        }
        register("economist.account.create.world") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to create accounts in specific worlds."
            children = listOf("economist.account.create.others")
        }

        register("economist.account.delete") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to delete accounts."
            children = listOf("economist.account")
        }
        register("economist.account.delete.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to delete accounts for others."
            children = listOf("economist.account.delete")
        }
        register("economist.account.delete.world") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to delete accounts in specific worlds."
            children = listOf("economist.account.delete.others")
        }

        register("economist.account.prune") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Prune accounts that haven't been used in a while."
            children = listOf("economist.account")
        }

        register("economist.account") {
            description = "Allows players to use accounts."
        }

        register("economist.bank.manage.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to manage others' banks."
            children = listOf(
                "economist.bank.deposit.others",
                "economist.bank.withdraw.others",
                "economist.bank.info.others",
                "economist.bank.manage"
            )
        }
        register("economist.bank.info.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to view others' banks info."
            children = listOf(
                "economist.bank.balance.others",
                "economist.bank.movements.others",
                "economist.bank.info",
                "economist.bank-top"
            )
        }
        register("economist.bank.balance.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to view others' banks balance."
            children = listOf("economist.bank.balance")
        }
        register("economist.bank.create.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to create banks for others."
            children = listOf("economist.bank.create")
        }
        register("economist.bank.delete.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to delete others' banks."
            children = listOf("economist.bank.delete")
        }
        register("economist.bank.movements.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to view others' banks movements."
            children = listOf("economist.bank.movements")
        }
        register("economist.bank.withdraw.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to withdraw money from others' banks."
            children = listOf("economist.bank.withdraw")
        }
        register("economist.bank.deposit.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to deposit money into others' banks."
            children = listOf("economist.bank.deposit")
        }
        register("economist.bank.transfer.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to transfer money between others' banks."
            children = listOf("economist.bank.transfer")
        }

        register("economist.bank.manage") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows players to manage their bank."
            children = listOf(
                "economist.bank.movements",
                "economist.bank.deposit",
                "economist.bank.transfer",
                "economist.bank.withdraw"
            )
        }

        register("economist.bank-top") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to view the bank top list."
            children = listOf("economist.bank.balance.others")
        }

        register("economist.bank.info") {
            description = "Allows players to view bank info."
            children = listOf("economist.bank.balance")
        }
        register("economist.bank.balance") {
            description = "Allows players to view their bank's balance."
            children = listOf("economist.bank")
        }
        register("economist.bank.create") {
            description = "Allows players to create banks."
            children = listOf("economist.bank")
        }
        register("economist.bank.delete") {
            description = "Allows players to delete their bank."
            children = listOf("economist.bank")
        }
        register("economist.bank.deposit") {
            description = "Allows players to deposit money into their bank."
            children = listOf("economist.bank")
        }
        register("economist.bank.withdraw") {
            description = "Allows players to withdraw money from their bank."
            children = listOf("economist.bank")
        }
        register("economist.bank.movements") {
            description = "Allows players to view bank movements."
            children = listOf("economist.bank")
        }
        register("economist.bank.transfer") {
            description = "Allows players to transfer money between banks."
            children = listOf("economist.bank")
        }

        register("economist.bank") {
            description = "Allows players to use banks."
        }

        register("economist.admin") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Admin permission to manage the economy."
            children = listOf(
                "economist.account.create.others",
                "economist.account.delete.others",
                "economist.account.prune",
                "economist.balance-top.world",
                "economist.pay.world",
                "economist.loan"
            )
        }

        register("economist.balance.world") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to see other players' balances in specific worlds."
            children = listOf("economist.balance.others")
        }
        register("economist.balance.others") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to see other players' balances."
            children = listOf("economist.balance")
        }
        register("economist.balance") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows players to see their balance."
        }

        register("economist.balance-top.world") {
            default = BukkitPluginDescription.Permission.Default.OP
            children = listOf("economist.balance-top", "economist.balance.world")
            description = "Allows players to see the balance top list of specific worlds."
        }
        register("economist.balance-top") {
            children = listOf("economist.balance.others")
            description = "Allows players to see the balance top list."
        }

        register("economist.pay.world") {
            default = BukkitPluginDescription.Permission.Default.OP
            children = listOf("economist.pay")
            description = "Allows players to make payments to specific worlds."
        }
        register("economist.pay") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows players to make payments."
        }

        register("economist.loan") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows players to loan money when making payments with insufficient funds."
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
        changelog = System.getenv("CHANGELOG")
        channel.set(if (isRelease) "Release" else "Snapshot")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms.register(Platforms.PAPER) {
            jar.set(tasks.shadowJar.flatMap { it.archiveFile })
            platformVersions.set(versions)
            dependencies {
                url("ServiceIO", "https://hangar.papermc.io/TheNextLvl/ServiceIO") {
                    required.set(false)
                }
            }
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("VQ63EMAa")
    changelog = System.getenv("CHANGELOG")
    versionType = if (isRelease) "release" else "beta"
    uploadFile.set(tasks.shadowJar)
    gameVersions.set(versions)
    syncBodyFrom.set(rootProject.file("README.md").readText())
    loaders.add("paper")
    loaders.add("folia")
    dependencies {
        optional.project("service-io")
    }
}
