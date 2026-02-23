package yay.evy.everest.vstuff.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class VServer extends ConfigBase {

    public final ConfigGroup server = group(0, "server", Comments.server);

    @Override
    public @NotNull String getName() {
        return "server";
    }

    private static class Comments {
        static String server = "VStuff server config, most values are here.";
    }
}
