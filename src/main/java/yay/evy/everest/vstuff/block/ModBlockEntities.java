package yay.evy.everest.vstuff.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.ropes.RopePulleyBlockEntity;
import yay.evy.everest.vstuff.vstuff;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, vstuff.MOD_ID);

    public static final RegistryObject<BlockEntityType<RopePulleyBlockEntity>> ROPE_PULLEY =
            BLOCK_ENTITIES.register("rope_pulley", () ->
                    BlockEntityType.Builder.of(RopePulleyBlockEntity::new,
                            ModBlocks.ROPE_PULLEY.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
