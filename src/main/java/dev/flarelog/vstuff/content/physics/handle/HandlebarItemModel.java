package dev.flarelog.vstuff.content.physics.handle;

import dev.flarelog.vstuff.index.VStuffBlocks;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class HandlebarItemModel {
    public static void register() {
        ItemProperties.register(
                VStuffBlocks.HANDLE.get().asItem(),
                new ResourceLocation("vstuff", "handlebar_color"),
                (stack, level, entity, seed) -> {
                    var tag = stack.getTagElement("BlockStateTag");
                    if (tag == null || !tag.contains("color")) {
                        return DyeColor.GRAY.getId() / 16f;
                    }

                    DyeColor dye = DyeColor.byName(tag.getString("color"), DyeColor.GRAY);
                    return dye.getId() / 16f;
                }
        );
    }
}