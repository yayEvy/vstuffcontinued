package yay.evy.everest.vstuff.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class VClient extends ConfigBase {

    public final ConfigGroup client = group(0, "client", Comments.client);

    public final ConfigFloat ropeThickness = f(0.25f, 0.01f, 1f, "ropeThickness", Comments.ropeThickness);
    public final ConfigBool ropeKnots = b(false, "ropeKnots", Comments.ropeKnots);

    @Override
    public @NotNull String getName() {
        return "client";
    }

    private static class Comments {
        static String client = "Clientside settings.";
        static String[] ropeKnots = new String[]{"Whether to render knots at rope endpoints. Currently a config & disabled by default due to not looking great with a lot of blocks, so TODO: find a better impl"};
        static String[] ropeThickness = new String[]{
                "[in Blocks]",
                "How thick a rope is rendered"
        };
    }


}
