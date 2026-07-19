package dev.flarelog.vstuff.index;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.RopeCutterItem;
import dev.flarelog.vstuff.content.ropes.RopeItem;
import dev.flarelog.vstuff.content.physics.ships.assembly.ExpendableAssemblerItem;
import dev.flarelog.vstuff.content.physics.physgrabber.PhysGrabberItem;
import dev.flarelog.vstuff.content.ropes.editor.RopeEditorItem;


public class VStuffItems {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    static {
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }

    public static final TagKey<Item> STYLING_AVAILABLE = TagKey.create(Registries.ITEM, VStuff.asResource("styling_available"));

    public static final ItemEntry<RopeItem> ROPE =
            REGISTRATE.item("rope", RopeItem::new)
                    .properties(p -> p
                            .stacksTo(64)
                    )
                    .tag(STYLING_AVAILABLE)
                    .model(AssetLookup.existingItemModel())
                    .register();

    public static final ItemEntry<RopeCutterItem> ROPE_CUTTER =
            REGISTRATE.item("rope_cutter", RopeCutterItem::new)
                    .properties(p -> p
                            .stacksTo(1)
                            .durability(238)
                    )
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
                            .rarity(Rarity.UNCOMMON)
                    )
                    .defaultModel()
                    .register();

    public static final ItemEntry<RopeEditorItem> CREATIVE_ROPE_EDITOR =
            REGISTRATE.item("creative_rope_editor", RopeEditorItem::new)
                    .properties(p -> p
                            .stacksTo(1)
                            .rarity(Rarity.RARE)
                    )
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