package yay.evy.everest.vstuff.events;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.ApiStatus;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeCategory;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeStyle;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @ApiStatus.Internal
    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                VStuffRegistries.ROPE_STYLES,
                RegistryRopeStyle.CODEC,
                RegistryRopeStyle.NETWORK_CODEC
        );
        event.dataPackRegistry(
                VStuffRegistries.ROPE_CATEGORIES,
                RegistryRopeCategory.CODEC,
                RegistryRopeCategory.CODEC
        );
    }
}
