package yay.evy.everest.vstuff.content.ropestyler.handler;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import yay.evy.everest.vstuff.content.ropestyler.RopeStylerScreen;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.index.VStuffKeys;
import yay.evy.everest.vstuff.util.EntityUtils;

public class RopeStyleMenuHandler {

    public static int COOLDOWN = 0;

    @SubscribeEvent
    public static void clientTick() {
        if (COOLDOWN > 0 && !VStuffKeys.ROPE_MENU.isPressed())
            COOLDOWN--;
    }

    @SubscribeEvent
    public static void onKeyInput(int key, boolean pressed) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        if (key != VStuffKeys.ROPE_MENU.getBoundCode() || !pressed)
            return;
        if (COOLDOWN > 0)
            return;
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        if (!EntityUtils.isHolding(player, VStuffItems.LEAD_CONSTRAINT_ITEM::isIn))
            return;

        ScreenOpener.open(new RopeStylerScreen(player));
    }

}
