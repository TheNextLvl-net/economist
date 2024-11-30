package net.thenextlvl.economist.version;

import core.paper.version.PaperHangarVersionChecker;
import core.version.SemanticVersion;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PluginVersionChecker extends PaperHangarVersionChecker<SemanticVersion> {
    public PluginVersionChecker(Plugin plugin) {
        super(plugin, "TheNextLvl", "Economist");
    }

    @Override
    public @Nullable SemanticVersion parseVersion(String version) {
        return SemanticVersion.parse(version);
    }
}
