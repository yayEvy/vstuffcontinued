package yay.evy.everest.vstuff.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class VClient extends ConfigBase {

    public final ConfigGroup client = group(0, "client", Comments.client);

    public final ConfigFloat ropeThickness = f(0.25f, 0.01f, 1f, "ropeThickness", Comments.ropeThickness);

    @Override
    public @NotNull String getName() {
        return "client";
    }

    private static class Comments {
        static String client = "Clientside settings.";
        static String[] ropeThickness = new String[]{
                "[in Blocks]",
                "How thick a rope is rendered"
        };
    }


}
