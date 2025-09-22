package yay.evy.everest.vstuff.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import yay.evy.everest.vstuff.content.constraint.LeadBreakItem;
import yay.evy.everest.vstuff.content.constraint.LeadConstraintItem;
import yay.evy.everest.vstuff.VStuff;


public class VStuffItems {

    static {
        VStuff.REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }

    public static final ItemEntry<LeadConstraintItem> LEAD_CONSTRAINT_ITEM =
            VStuff.REGISTRATE.item("lead_constraint_item", LeadConstraintItem::new).register();

    public static final ItemEntry<LeadBreakItem> LEAD_BREAK_ITEM =
            VStuff.REGISTRATE.item("lead_break_item", LeadBreakItem::new).register();

    public static final ItemEntry<Item> ROPE_ANDESITE =
            VStuff.REGISTRATE.item("rope_andesite", Item::new)
                    .properties(p -> p.stacksTo(64)) // normal stack size
                    .register();

    public static void register() {}
}
