package yay.evy.everest.vstuff.content.pulley;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PhysPulleyItem extends BlockItem {

    public PhysPulleyItem(Block block, Properties properties) {
        super(block, properties);
    }
    public static void setWaitingPulley(Player player, PhysPulleyBlockEntity pulley) {
        waitingPulleys.put(player.getUUID(), pulley);
    }

    public static void clearWaitingPulley(Player player) {
        waitingPulleys.remove(player.getUUID());
    }

    public static PhysPulleyBlockEntity getWaitingPulley(Player player) {
        return waitingPulleys.get(player.getUUID());
    }
    private static final Map<UUID, PhysPulleyBlockEntity> waitingPulleys = new HashMap<>();


    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal("§7A mechanical pulley that extends and retracts rope"));
            tooltip.add(Component.literal("§7using Create mod rotation"));
        } else {
            tooltip.add(Component.literal("[§7Hold §eShift §7for info§r]")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }



}
