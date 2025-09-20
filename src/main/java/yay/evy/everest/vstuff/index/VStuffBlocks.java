package yay.evy.everest.vstuff.index;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Blocks;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlock;

public class VStuffBlocks {

    static {
        VStuff.REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }

    public static final BlockEntry<RotationalThrusterBlock> ROTATIONAL_THRUSTER =
            VStuff.REGISTRATE.block("rotational_thruster", RotationalThrusterBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .simpleItem() // this generates the BlockItem automatically
                    .register();

    public static void register() {}
}
