package yay.evy.everest.vstuff.blocks;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VStuff.MOD_ID);


    public static final BlockEntry<RotationalThrusterBlock> ROTATIONAL_THRUSTER =
            VStuff.REGISTRATE.block("rotational_thruster", RotationalThrusterBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .simpleItem() // this generates the BlockItem automatically
                    .register();




    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
