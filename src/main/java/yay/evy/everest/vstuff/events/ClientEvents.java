package yay.evy.everest.vstuff.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yay.evy.everest.vstuff.content.rope.styler.handler.RopeStyleMenuHandler;

@EventBusSubscriber(value = Dist.CLIENT, modid = "vstuff")
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isGameActive()) return;

        if (event.phase == Phase.START) {
            RopeStyleMenuHandler.clientTick();
            return;
        }

        //NewRopeUtils.clientTick();
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

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        //event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "rope_overlay", RopeTextOverlay.INSTANCE);
    }

}
