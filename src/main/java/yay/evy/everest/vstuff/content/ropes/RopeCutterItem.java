package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.VStuffConfig;
import yay.evy.everest.vstuff.internal.RopeStyle;
import yay.evy.everest.vstuff.internal.RopeStyleManager;
import yay.evy.everest.vstuff.internal.network.NetworkHandler;
import yay.evy.everest.vstuff.index.VStuffItems;
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

        Integer targetConstraintId = RopeUtils.findTargetedLead(serverLevel, player);
        if (targetConstraintId == null) return InteractionResultHolder.pass(itemStack);

        try {
            ReworkedRope data = RopeManager.getActiveRopes().get(targetConstraintId);
            if (data == null) return InteractionResultHolder.pass(itemStack);

            RopeStyle.RenderStyle style = RopeStyleManager.get(data.style).renderStyle();

            Component notif = style == RopeStyle.RenderStyle.CHAIN
                    ? Component.translatable("vstuff.message.chain_break")
                    : Component.translatable("vstuff.message.rope_break");
            player.displayClientMessage(notif, true);

            if (VStuffConfig.ROPE_SOUNDS.get()) {
                var sound = (style == RopeStyle.RenderStyle.CHAIN)
                        ? SoundEvents.CHAIN_BREAK
                        : SoundEvents.LEASH_KNOT_BREAK;
                serverLevel.playSound(
                        null,
                        player.blockPosition(),
                        sound,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );
            }

            data.removeJoint(serverLevel);

            if (!player.isCreative()) {
                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                player.drop(new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get()), false);
            }

            return InteractionResultHolder.success(itemStack);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return InteractionResultHolder.pass(itemStack);
    }

}