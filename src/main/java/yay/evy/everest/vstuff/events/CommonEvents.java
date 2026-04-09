package yay.evy.everest.vstuff.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeCategoryReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeRestyleReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeStyleReloadListener;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {
    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        System.out.println("reload listener");
        event.addListener(new RopeStyleReloadListener());
        event.addListener(new RopeCategoryReloadListener());
        event.addListener(new RopeRestyleReloadListener());
    }
}
