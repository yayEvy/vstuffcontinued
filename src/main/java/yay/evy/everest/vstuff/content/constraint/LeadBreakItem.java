package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.sound.RopeSoundHandler;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeadBreakItem extends Item {
    public LeadBreakItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Integer targetConstraintId = RopeUtil.findTargetedLead(serverLevel, player);
            if (targetConstraintId != null) {
                try {
                    VStuff.LOGGER.info("Attempting to remove constraint: {}", targetConstraintId);

                    VSGameUtilsKt.getShipObjectWorld(serverLevel).removeConstraint(targetConstraintId);
                    ConstraintTracker.RopeConstraintData data = ConstraintTracker.getActiveConstraints().get(targetConstraintId);
                    ConstraintTracker.removeConstraintWithPersistence(serverLevel, targetConstraintId);
                    NetworkHandler.sendConstraintRemove(targetConstraintId);
                    forceRemoveConstraint(serverLevel, targetConstraintId);

                    RopeStyles.PrimitiveRopeStyle style = (data != null)
                            ? data.style.getBasicStyle()
                            : RopeStyles.PrimitiveRopeStyle.NORMAL;

                    Component notif = (style == RopeStyles.PrimitiveRopeStyle.CHAIN)
                            ? Component.translatable("vstuff.message.chain_break")
                            : Component.translatable("vstuff.message.rope_break");

                    player.displayClientMessage(notif, true);

                    // config
                    if (VstuffConfig.ROPE_SOUNDS.get()) {

                        var sound = (style == RopeStyles.PrimitiveRopeStyle.CHAIN)
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




                    if (data != null && data.sourceBlockPos != null) {
                        ConstraintTracker.cleanupOrphanedConstraints(serverLevel, data.sourceBlockPos);
                    }

                    VStuff.LOGGER.info("Removed constraint (1st attempt): {}", targetConstraintId);

                    if (ConstraintTracker.getActiveConstraints().containsKey(targetConstraintId)) {
                        VStuff.LOGGER.warn("Constraint {} still present, retrying...", targetConstraintId);

                        VSGameUtilsKt.getShipObjectWorld(serverLevel).removeConstraint(targetConstraintId);
                        ConstraintTracker.removeConstraintWithPersistence(serverLevel, targetConstraintId);
                        NetworkHandler.sendConstraintRemove(targetConstraintId);
                        forceRemoveConstraint(serverLevel, targetConstraintId);
                        ConstraintPersistence persistence = ConstraintPersistence.get(serverLevel);
                        persistence.saveNow(serverLevel);


                        VStuff.LOGGER.info("Removed constraint (2nd attempt): {}", targetConstraintId);
                    }

                    if (!player.getAbilities().instabuild) {
                        itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                        player.drop(new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get()), false);
                    }

                    return InteractionResultHolder.success(itemStack);
                } catch (Exception e) {
                    VStuff.LOGGER.error("Error removing constraint: {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return InteractionResultHolder.pass(itemStack);
    }

    private void forceRemoveConstraint(ServerLevel level, int id) {
        ConstraintTracker.RopeConstraintData data = ConstraintTracker.getActiveConstraints().get(id);

        try {
            VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(id);
        } catch (Exception ignored) {}

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        persistence.saveNow(level);



        ConstraintTracker.removeConstraintWithPersistence(level, id);
        ConstraintTracker.getActiveConstraints().remove(id);
        NetworkHandler.sendConstraintRemove(id);

        if (data != null) {
            if (data.anchorBlockPosA != null) {
                ConstraintTracker.cleanupOrphanedConstraints(level, data.anchorBlockPosA);
            }
            if (data.anchorBlockPosB != null) {
                ConstraintTracker.cleanupOrphanedConstraints(level, data.anchorBlockPosB);
            }
        }
    }
}
