import org.jspecify.annotations.NullMarked;

@NullMarked
module economist.main {
    requires com.google.gson;
    requires core.files;
    requires core.paper;
    requires de.chojo.sadu.sqlite;
    requires de.chojo.sadu.updater;
    requires dev.faststats.bukkit;
    requires dev.faststats.core;
    requires java.sql;
    requires net.kyori.adventure.key;
    requires net.kyori.adventure.text.minimessage;
    requires net.kyori.adventure.text.serializer.plain;
    requires net.kyori.adventure;
    requires net.thenextlvl.economist;
    requires net.thenextlvl.i18n;
    requires net.thenextlvl.version.modrinth.paper;
    requires net.thenextlvl.version;
    requires org.bukkit;

    requires static org.jetbrains.annotations;
    requires static org.jspecify;
}