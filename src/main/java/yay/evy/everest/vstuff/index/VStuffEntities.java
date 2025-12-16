package yay.evy.everest.vstuff.index;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropethrower.RopeThrowerEntity;

public class VStuffEntities { // we would use create registrate but it crashes loading
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, VStuff.MOD_ID);

    public static final RegistryObject<EntityType<RopeThrowerEntity>> ROPE_THROWER =
            ENTITY_TYPES.register("rope_thrower", () -> EntityType.Builder.of(RopeThrowerEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("rope_thrower"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}