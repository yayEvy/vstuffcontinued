package yay.evy.everest.vstuff.ropes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class RopePulleyItem extends BlockItem {

    public RopePulleyItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7A mechanical pulley that extends and retracts rope"));
        tooltip.add(Component.literal("§7using Create mod rotation power"));
        tooltip.add(Component.literal("§6Clockwise: §7Extend rope"));
        tooltip.add(Component.literal("§6Counter-clockwise: §7Retract rope"));
        tooltip.add(Component.literal("§6Shift-click: §7Reset rope length"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
