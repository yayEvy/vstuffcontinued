package yay.evy.everest.vstuff.impl.data;

import net.minecraft.data.DataGenerator;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.api.data.DataProviderContext;
import yay.evy.everest.vstuff.api.data.RopeCategoryProvider;

public class RopeCategoryProviderImpl extends RopeCategoryProvider {

    public RopeCategoryProviderImpl(DataGenerator generator) {
        super(generator);
    }

    @Override
    public void categories(@NotNull DataProviderContext ctx) {
        category(ctx, "Basic Styles", 0);
        category(ctx, "Wool Styles", 1);
        category(ctx, "Dyed Styles", 2);
        category(ctx, "Pride Styles", 3);
        category(ctx, "Log Styles", 4);
        category(ctx, "Merry VStuffmas", 5);
    }

    @Override
    public @NotNull String getName() {
        return "vstuff_ropecategories";
    }
}
