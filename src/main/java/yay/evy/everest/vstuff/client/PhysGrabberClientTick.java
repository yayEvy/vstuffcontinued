package yay.evy.everest.vstuff.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles updating the PhysGrabber target every client tick.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PhysGrabberClientTick {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PhysGrabberClientHandler.tickClient(mc, mc.player);
        }
    }
}
