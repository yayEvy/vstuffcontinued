package yay.evy.everest.vstuff.impl.registry;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.ApiStatus;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeCategory;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeStyle;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class VStuffRegistriesImpl {

    @ApiStatus.Internal
    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                VStuffRegistries.STYLES,
                RegistryRopeStyle.CODEC,
                RegistryRopeStyle.CODEC
        );
        event.dataPackRegistry(
                VStuffRegistries.CATEGORIES,
                RegistryRopeCategory.CODEC,
                RegistryRopeCategory.CODEC
        );
    }
}
