package yay.evy.everest.vstuff.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleMenuHandler;

@EventBusSubscriber(value = Dist.CLIENT, modid = "vstuff")
public class ClientEvents {


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.START)
            ClientEvents.onClientTickStart(Minecraft.getInstance());
        else if (event.phase == Phase.END)
            ClientEvents.onClientTickEnd(Minecraft.getInstance());
    }

    public static void onClientTickStart(Minecraft mc) {
        if (isGameActive()) {
            RopeStyleMenuHandler.clientTick();
        }
    }

    public static void onClientTickEnd(Minecraft mc) {

    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        int key = event.getKey();
        boolean pressed = event.getAction() != 0;
        RopeStyleMenuHandler.onKeyInput(key, pressed);
    }

}
