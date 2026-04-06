package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.createmod.catnip.lang.Lang;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.arrow.RopeArrowEntity;
import yay.evy.everest.vstuff.content.ropes.arrow.RopeArrowRenderer;
import yay.evy.everest.vstuff.content.ropes.thrower.RopeThrowerEntity;

public class VStuffEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, VStuff.MOD_ID);

    public static final RegistryObject<EntityType<RopeThrowerEntity>> ROPE_THROWER =
            ENTITY_TYPES.register("rope_thrower", () -> EntityType.Builder.<RopeThrowerEntity>of(RopeThrowerEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("rope_thrower"));

        public static final RegistryObject<EntityType<RopeArrowEntity>> ROPE_ARROW =
            ENTITY_TYPES.register("rope_arrow", () -> EntityType.Builder.<RopeArrowEntity>of(RopeArrowEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("rope_arrow"));




    //todo rope arrow + use create registrate for entities

    /// I have done it, I am free, my shackles doth been removed! I am moving to nevada -Bry

    private static <T extends Entity> CreateEntityBuilder<T, ?> register(String name, EntityType.EntityFactory<T> factory,
                                                                         NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer,
                                                                         MobCategory group, int range, int updateFrequency, boolean sendVelocity, boolean immuneToFire,
                                                                         NonNullConsumer<EntityType.Builder<T>> propertyBuilder) {
        String id = Lang.asId(name);
        return (CreateEntityBuilder<T, ?>) VStuff.registrate()
            .entity(id, factory, group)
            .properties(b -> b.setTrackingRange(range)
                .setUpdateInterval(updateFrequency)
                .setShouldReceiveVelocityUpdates(sendVelocity))
            .properties(propertyBuilder)
            .properties(b -> {
                if (immuneToFire)
                    b.fireImmune();
            })
            .renderer(renderer);
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}