package yay.evy.everest.vstuff.content.constraintrework.items;

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
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.content.constraintrework.RopeManager;
import yay.evy.everest.vstuff.content.constraintrework.ropes.AbstractRope;
import yay.evy.everest.vstuff.content.constraintrework.ropes.RopeUtils;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.util.RopeStyles;

public class RopeCutterItem extends Item {
    public RopeCutterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack heldStack = pPlayer.getItemInHand(pUsedHand);

        if (pLevel instanceof ServerLevel serverLevel) {
            Integer targetRopeId = RopeUtils.findRope(serverLevel, pPlayer);
            if (targetRopeId != null) {
                AbstractRope rope = RopeManager.getActiveRopes().get(targetRopeId);

                rope.removeJoint(serverLevel);

                RopeManager.REMOVE(serverLevel, targetRopeId);

                boolean isChain = rope.style.getBasicStyle() == RopeStyles.PrimitiveRopeStyle.CHAIN;

                Component notif = isChain
                        ? Component.translatable("vstuff.message.chain_break")
                        : Component.translatable("vstuff.message.rope_break");

                pPlayer.displayClientMessage(notif, true);

                if (VstuffConfig.ROPE_SOUNDS.get()) {
                    var sound = isChain
                            ? SoundEvents.CHAIN_BREAK
                            : SoundEvents.LEASH_KNOT_BREAK;

                    serverLevel.playSound(
                            null,
                            pPlayer.blockPosition(),
                            sound,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                }

                if (!pPlayer.isCreative()) {
                    heldStack.hurtAndBreak(1, pPlayer, (p) -> p.broadcastBreakEvent(pUsedHand));
                    pPlayer.drop(new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get()), false);
                }

                return InteractionResultHolder.success(heldStack);
            }
        }
        return InteractionResultHolder.fail(heldStack);
    }
}
