package yay.evy.everest.vstuff.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.ropes.LeadBreakItem;
import yay.evy.everest.vstuff.ropes.LeadConstraintItem;
import yay.evy.everest.vstuff.blocks.ModBlocks;



public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "vstuff");

    public static final RegistryObject<Item> LEAD_CONSTRAINT_ITEM = ITEMS.register("lead_constraint_item",
            () -> new LeadConstraintItem());

    public static final RegistryObject<Item> LEAD_BREAK_ITEM = ITEMS.register("lead_break_item",
            () -> new LeadBreakItem());




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
