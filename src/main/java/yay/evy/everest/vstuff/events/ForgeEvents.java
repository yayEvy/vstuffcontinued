package yay.evy.everest.vstuff.events;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.data.RopeStyleCategoryReloadListener;
import yay.evy.everest.vstuff.internal.data.RopeStyleReloadListener;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {

        event.addListener(new RopeStyleReloadListener());
        event.addListener(new RopeStyleCategoryReloadListener());
    }
}
