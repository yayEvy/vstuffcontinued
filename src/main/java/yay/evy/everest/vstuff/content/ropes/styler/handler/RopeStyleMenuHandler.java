package yay.evy.everest.vstuff.content.ropes.styler.handler;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import yay.evy.everest.vstuff.content.ropes.styler.RopeStylerScreen;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.index.VStuffKeys;
import yay.evy.everest.vstuff.internal.utility.EntityUtils;

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

        boolean holdingLead = EntityUtils.isHolding(player, VStuffItems.LEAD_CONSTRAINT_ITEM::isIn);
        boolean holdingThrower = EntityUtils.isHolding(player, VStuffItems.ROPE_THROWER_ITEM::isIn);

        if (!holdingLead && !holdingThrower)
            return;


        ScreenOpener.open(new RopeStylerScreen(player));
    }

}
