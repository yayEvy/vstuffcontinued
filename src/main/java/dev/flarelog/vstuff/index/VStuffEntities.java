package dev.flarelog.vstuff.index;

import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.createmod.catnip.lang.Lang;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.arrow.RopeArrowEntity;
import dev.flarelog.vstuff.content.ropes.arrow.RopeArrowRenderer;

public class VStuffEntities {

    public static final EntityEntry<RopeArrowEntity> ROPE_ARROW =
        register("rope_arrow", RopeArrowEntity::new, () -> RopeArrowRenderer::new, MobCategory.MISC, 4, 20, true, false, RopeArrowEntity::build).register();

    //todo create registrate of doom and despair

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

    public static void register() {}
}