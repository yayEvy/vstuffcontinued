package dev.flarelog.vstuff.content.physics.handle;

import dev.flarelog.vstuff.index.VStuffBlocks;
import dev.flarelog.vstuff.index.VStuffRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

public class HandlebarDyeRecipe extends CustomRecipe {
    public HandlebarDyeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean foundHandle = false;
        boolean foundDye = false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(VStuffBlocks.HANDLE.get().asItem()) && !foundHandle) {
                foundHandle = true;
            } else if (stack.getItem() instanceof DyeItem && !foundDye) {
                foundDye = true;
            } else {
                return false;
            }
        }

        return foundHandle && foundDye;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack handle = ItemStack.EMPTY;
        DyeItem dye = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.is(VStuffBlocks.HANDLE.get().asItem())) {
                handle = stack;
            } else if (stack.getItem() instanceof DyeItem dyeItem) {
                dye = dyeItem;
            }
        }

        if (handle.isEmpty() || dye == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = handle.copy();
        result.setCount(1);
        result.getOrCreateTagElement("BlockStateTag")
                .putString("color", dye.getDyeColor().getName());

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(VStuffBlocks.HANDLE.get());
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return VStuffRecipeSerializers.HANDLEBAR_DYE.get();
    }
}
