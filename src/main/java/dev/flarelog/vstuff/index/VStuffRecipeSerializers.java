
package dev.flarelog.vstuff.index;

import dev.flarelog.vstuff.content.physics.handle.HandlebarDyeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class VStuffRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, "vstuff");

    public static final RegistryObject<RecipeSerializer<HandlebarDyeRecipe>> HANDLEBAR_DYE =
            SERIALIZERS.register("handlebar_dye",
                    () -> new SimpleCraftingRecipeSerializer<>(HandlebarDyeRecipe::new));

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
