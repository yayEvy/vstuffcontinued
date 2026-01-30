package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class EntityUtils {

    public static boolean isHolding(Player player, Predicate<ItemStack> predicate) {
        return predicate.test(player.getItemInHand(InteractionHand.MAIN_HAND))
                || predicate.test(player.getItemInHand(InteractionHand.OFF_HAND));
    }

    public static boolean isHoldingItem(Player player, Predicate<Item> predicate) {
        return isHolding(player, (stack) -> predicate.test(stack.getItem()));
    }

}
