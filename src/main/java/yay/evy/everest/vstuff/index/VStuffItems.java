package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Rarity;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.rope.ropes.RopeCutterItem;
import yay.evy.everest.vstuff.content.rope.ropes.RopeItem;
import yay.evy.everest.vstuff.content.physgrabber.PhysGrabberItem;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.EmptyEnergyCoreItem;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.EnergyCoreItem;
import yay.evy.everest.vstuff.content.rope.ropethrower.RopeThrowerItem;


public class VStuffItems {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    static {
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }


    public static final ItemEntry<RopeItem> ROPE_ITEM =
            REGISTRATE.item("rope_item", RopeItem::new)
                    .properties(p -> p
                            .stacksTo(64))
                    .defaultModel()
                    .register();

    public static final ItemEntry<RopeThrowerItem> ROPE_THROWER_ITEM =
            REGISTRATE.item("rope_thrower", RopeThrowerItem::new)
                    .properties(p -> p
                            .stacksTo(64))
                    .defaultModel()
                    .register();

    public static final ItemEntry<RopeCutterItem> ROPE_CUTTER_ITEM =
            REGISTRATE.item("rope_cutter_item", RopeCutterItem::new)
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

    public static final ItemEntry<EmptyEnergyCoreItem> EMPTY_ENERGY_CORE =
            REGISTRATE.item("empty_energy_core", EmptyEnergyCoreItem::new)
                    .properties(p -> p
                            .stacksTo(64))
                    .model(AssetLookup.itemModelWithPartials())
                    .register();

    public static final ItemEntry<EnergyCoreItem> ENERGY_CORE =
            REGISTRATE.item("energy_core", EnergyCoreItem::new)
                    .properties(p -> p
                            .stacksTo(64)
                            .rarity(Rarity.UNCOMMON))
                    .model(AssetLookup.itemModelWithPartials())
                    .register();



    public static void register() {}
}
