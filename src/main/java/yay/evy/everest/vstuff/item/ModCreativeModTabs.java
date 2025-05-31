package yay.evy.everest.vstuff.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.block.ModBlocks;
import yay.evy.everest.vstuff.vstuff;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, vstuff.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VSTUFF_TAB = CREATIVE_MODE_TABS.register("vstuff_tab",
            () -> CreativeModeTab.builder()
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
