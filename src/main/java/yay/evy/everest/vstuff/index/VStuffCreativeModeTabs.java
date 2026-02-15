package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.VStuff;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;


public class VStuffCreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VStuff.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VSTUFF_MAIN = CREATIVE_TAB_REGISTER.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetabs.main"))
                    .icon(VStuffItems.ROPE::asStack)
                    .displayItems(new MainDisplayItemsGen())
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_TAB_REGISTER.register(modEventBus);
    }

    public static class MainDisplayItemsGen implements CreativeModeTab.DisplayItemsGenerator {

        private List<Item> collectBlocks(RegistryObject<CreativeModeTab> tab, Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();
            for (RegistryEntry<Block> entry : VStuff.REGISTRATE.getAll(Registries.BLOCK)) {
                if (!CreateRegistrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get().asItem();
                if (item == Items.AIR)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            items = new ReferenceArrayList<>(new ReferenceLinkedOpenHashSet<>(items));
            return items;
        }

        private List<Item> collectItems(RegistryObject<CreativeModeTab> tab, Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();

            for (RegistryEntry<Item> entry : VStuff.REGISTRATE.getAll(Registries.ITEM)) {
                if (!CreateRegistrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            return items;
        }

        private static void outputAll(CreativeModeTab.Output output, List<Item> items) {
            for (Item item : items) {
                output.accept(item);
            }
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
            List<Item> items = new LinkedList<>();
            items.addAll(collectBlocks(VSTUFF_MAIN, (item) -> {
                return false;
            }));
            items.addAll(collectItems(VSTUFF_MAIN, (item) -> {
                return false;
            }));

            outputAll(output, items);
        }
    }
}
