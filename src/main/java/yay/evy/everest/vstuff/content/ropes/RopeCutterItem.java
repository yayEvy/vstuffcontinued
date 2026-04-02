package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.RopeStyleManager;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

public class RopeCutterItem extends Item {
    public RopeCutterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(itemStack);
        }

        Integer targetRope = RopeUtils.findTargetedLead(serverLevel, player);
        if (targetRope == null) return InteractionResultHolder.pass(itemStack);

        try {
            ReworkedRope rope = RopeFactory.removeRope(serverLevel, targetRope);

            boolean chain = (rope.style.ropeRenderType() == RopeStyleManager.RopeRenderType.CHAIN);

            player.displayClientMessage(
                    Component.translatable("vstuff.rope." + (chain ? "chain" : "rope") + "_break"),
                    true
            );

            RopeUtils.playBreakSound(serverLevel, player.blockPosition(), chain);


            if (!player.isCreative()) {
                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                player.drop(new ItemStack(VStuffItems.ROPE.get()), false);
            }

            return InteractionResultHolder.success(itemStack);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return InteractionResultHolder.pass(itemStack);
    }

}