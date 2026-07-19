package dev.flarelog.vstuff.events;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.ApiStatus;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.content.ropes.style.RopeCategory;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @ApiStatus.Internal
    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                VStuffRegistries.ROPE_STYLE,
                RopeStyle.CODEC,
                RopeStyle.CODEC
        );

        event.dataPackRegistry(
                VStuffRegistries.ROPE_CATEGORY,
                RopeCategory.DIRECT_CODEC,
                RopeCategory.DIRECT_CODEC
        );
    }
}