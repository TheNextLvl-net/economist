package net.thenextlvl.economist.plugin.version;

import net.thenextlvl.version.SemanticVersion;
import net.thenextlvl.version.modrinth.paper.PaperModrinthVersionChecker;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

public class PluginVersionChecker extends PaperModrinthVersionChecker<SemanticVersion> {
    public PluginVersionChecker(final Plugin plugin) {
        super(plugin, "VQ63EMAa");
    }

    @Override
    public @Nullable SemanticVersion parseVersion(final String version) {
        return SemanticVersion.parse(version);
    }
}
