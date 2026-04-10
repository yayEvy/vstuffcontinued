package yay.evy.everest.vstuff.impl.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.api.data.DataProviderContext;
import yay.evy.everest.vstuff.api.data.RopeStyleProvider;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static yay.evy.everest.vstuff.VStuff.mcResource;
import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;
import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.LOGS;

public class RopeStyleProviderImpl extends RopeStyleProvider {
    public RopeStyleProviderImpl(DataGenerator generator) {
        super(generator);
    }

    @Override
    public void styles(@NotNull DataProviderContext ctx) {
        ropeStyle(ctx, "Normal", "basic_styles", "normal", RopeStyleProvider::modResource);
        ropeStyle(ctx, "Chain", "basic_styles", "chain", RopeStyleProvider::modResource);

        ropeStyles(ctx, WOOLS, "wool_styles", "normal", RopeStyleProvider::block);
        ropeStyles(ctx, LOGS, "log_styles", "normal", RopeStyleProvider::block);
        ropeStyles(ctx, DYE_COLORS, "dyed_styles", "solid_colour", RopeStyleProvider::colorParams);

        ropeStyles(ctx, List.of("Pride", "Gay", "Lesbian", "Bisexual", "Transgender", "Nonbinary", "Asexual"), "pride_styles", "normal", RopeStyleProvider::modResource);

        ropeStyle(ctx, "Candycane", "merry_vstuffmas", "normal", RopeStyleProvider::modResource);
        ropeStyle(ctx, "Christmas Tree", "merry_vstuffmas", "normal", RopeStyleProvider::modResource);
    }


    @Override
    public @NotNull String getName() {
        return "vstuff_ropestyles";
    }
}
