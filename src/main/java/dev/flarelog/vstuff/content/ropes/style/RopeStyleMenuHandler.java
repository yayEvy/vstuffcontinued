package dev.flarelog.vstuff.content.ropes.style;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.flarelog.vstuff.index.VStuffItems;
import dev.flarelog.vstuff.index.VStuffKeys;

import java.util.function.Predicate;

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

        InteractionHand hand = holdingInHand(player, stack -> stack.is(VStuffItems.STYLING_AVAILABLE));
        if (hand == null) return;

        ScreenOpener.open(new RopeStylerScreen(player));
    }

    static InteractionHand holdingInHand(Player player, Predicate<ItemStack> predicate) {
        if (predicate.test(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            return InteractionHand.MAIN_HAND;
        } else if (predicate.test(player.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionHand.OFF_HAND;
        } else {
            return null;
        }
    }

}
