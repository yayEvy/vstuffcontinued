package yay.evy.everest.vstuff.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.blocks.ModBlocks;
import yay.evy.everest.vstuff.vstuff;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, vstuff.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VSTUFF_TAB = CREATIVE_MODE_TABS.register("vstuff_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LEAD_CONSTRAINT_ITEM.get()))
                    .title(Component.translatable("creativetab.vstuff_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        // Blocks



                        // Items
                        pOutput.accept(ModItems.LEAD_CONSTRAINT_ITEM.get());
                        pOutput.accept(ModItems.LEAD_BREAK_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
