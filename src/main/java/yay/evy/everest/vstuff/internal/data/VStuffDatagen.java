package yay.evy.everest.vstuff.internal.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VStuffDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        VStuff.LOGGER.info("Running VStuff datagen");
        DataGenerator generator = event.getGenerator();

        generator.addProvider(event.includeServer(), new RopeStyleProvider(generator));
        generator.addProvider(event.includeServer(), new RopeStyleCategoryProvider(generator));
    }
}
