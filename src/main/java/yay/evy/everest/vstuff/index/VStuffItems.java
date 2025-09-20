package yay.evy.everest.vstuff.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import yay.evy.everest.vstuff.item.LeadBreakItem;
import yay.evy.everest.vstuff.item.LeadConstraintItem;
import yay.evy.everest.vstuff.VStuff;


public class VStuffItems {

    static {
        VStuff.REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }

    public static final ItemEntry<LeadConstraintItem> LEAD_CONSTRAINT_ITEM =
            VStuff.REGISTRATE.item("lead_constraint_item", LeadConstraintItem::new).register();

    public static final ItemEntry<LeadBreakItem> LEAD_BREAK_ITEM =
            VStuff.REGISTRATE.item("lead_break_item", LeadBreakItem::new).register();


    public static void register(IEventBus modEventBus) {}
}
