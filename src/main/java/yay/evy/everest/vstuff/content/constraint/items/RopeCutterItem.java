package yay.evy.everest.vstuff.content.constraint.items;

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
import yay.evy.everest.vstuff.content.constraint.MasterOfRopes;
import yay.evy.everest.vstuff.content.constraint.ropes.AbstractRope;
import yay.evy.everest.vstuff.content.constraint.ropes.RopeUtils;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.util.RopeStyles;

public class RopeCutterItem extends Item {
    public RopeCutterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack heldStack = player.getItemInHand(useHand);

        if (level instanceof ServerLevel serverLevel) {
            Integer targetRopeId = RopeUtils.findRope(serverLevel, player);
            if (targetRopeId != null) {
                AbstractRope rope = MasterOfRopes.getAllActiveRopes().get(targetRopeId);

                rope.removeJoint(serverLevel);

                MasterOfRopes.REMOVE(serverLevel, targetRopeId);

                boolean isChain = rope.style.getBasicStyle() == RopeStyles.PrimitiveRopeStyle.CHAIN;

                Component notif = isChain
                        ? Component.translatable("vstuff.message.chain_break")
                        : Component.translatable("vstuff.message.rope_break");

                player.displayClientMessage(notif, true);

                if (VstuffConfig.ROPE_SOUNDS.get()) {
                    var sound = isChain
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

                if (!player.isCreative()) {
                    heldStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(useHand));
                    player.drop(new ItemStack(VStuffItems.ROPE_ITEM.get()), false);
                }

                return InteractionResultHolder.success(heldStack);
            }
        }
        return InteractionResultHolder.fail(heldStack);
    }
}
