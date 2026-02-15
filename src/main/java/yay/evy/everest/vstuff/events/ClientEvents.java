package yay.evy.everest.vstuff.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yay.evy.everest.vstuff.content.ropes.styler.handler.RopeStyleMenuHandler;
import yay.evy.everest.vstuff.index.VStuffEntities;

@EventBusSubscriber(value = Dist.CLIENT, modid = "vstuff")
public class ClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(VStuffEntities.ROPE_THROWER.get(), ThrownItemRenderer::new);
    }


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.START)
            onClientTickStart(Minecraft.getInstance());
        else if (event.phase == Phase.END)
            onClientTickEnd(Minecraft.getInstance());
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
