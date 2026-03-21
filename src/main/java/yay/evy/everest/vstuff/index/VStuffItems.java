package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.ReworkedRopeItem;
import yay.evy.everest.vstuff.content.ropes.RopeCutterItem;
import yay.evy.everest.vstuff.content.ships.assembly.ExpendableAssemblerItem;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.physgrabber.PhysGrabberItem;
import yay.evy.everest.vstuff.content.ropes.editor.RopeEditorItem;
import yay.evy.everest.vstuff.content.ropes.thrower.RopeThrowerItem;


public class VStuffItems {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    static {
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }


    public static final ItemEntry<ReworkedRopeItem> ROPE =
        REGISTRATE.item("rope", ReworkedRopeItem::new)
            .properties(p -> p
                .stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<RopeThrowerItem> ROPE_THROWER =
        REGISTRATE.item("rope_thrower", RopeThrowerItem::new)
            .properties(p -> p
                .stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<RopeCutterItem> ROPE_CUTTER =
        REGISTRATE.item("rope_cutter", RopeCutterItem::new)
            .properties(p -> p
            .stacksTo(1)
            .durability(238))
            .defaultModel()
            .register();

    public static final ItemEntry<PhysGrabberItem> PHYS_GRABBER =
        REGISTRATE.item("phys_grabber", PhysGrabberItem::new)
            .properties(p -> p
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
                .durability(1096))
            .model(AssetLookup.itemModelWithPartials())
            .register();

    public static final ItemEntry<ExpendableAssemblerItem> EXPENDABLE_ASSEMBLER =
        REGISTRATE.item("expendable_assembler", ExpendableAssemblerItem::new)
            .properties(p -> p
                .stacksTo(16)
                .rarity(Rarity.UNCOMMON))
            .defaultModel()
            .register();

    public static final ItemEntry<RopeEditorItem> CREATIVE_ROPE_EDITOR =
        REGISTRATE.item("creative_rope_editor", RopeEditorItem::new)
            .properties(p -> p
                .stacksTo(1)
                .rarity(Rarity.RARE))
            .defaultModel()
            .register();

    /*
    public static final ItemEntry<Item> REFINED_LEVITUFF =
            REGISTRATE.item("refined_levituff", Item::new)
                    .properties(p -> p.rarity(Rarity.UNCOMMON))
                    .defaultModel()
                    .register();

     */

    public static void register() {}
}
