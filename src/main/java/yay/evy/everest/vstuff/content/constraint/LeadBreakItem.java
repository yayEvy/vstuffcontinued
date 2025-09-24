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
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.sound.RopeSoundHandler;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.List;
import java.util.Map;

public class LeadBreakItem extends Item {
    public LeadBreakItem(Properties pProperties) {
        super(new Properties().stacksTo(1).durability(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Integer targetConstraintId = findTargetedLead(serverLevel, player);
            if (targetConstraintId != null) {
                try {
                    System.out.println("Attempting to remove constraint: " + targetConstraintId);

                    VSGameUtilsKt.getShipObjectWorld(serverLevel).removeConstraint(targetConstraintId);
                    ConstraintTracker.RopeConstraintData data = ConstraintTracker.getActiveConstraints().get(targetConstraintId);
                    ConstraintTracker.removeConstraintWithPersistence(serverLevel, targetConstraintId);
                    NetworkHandler.sendConstraintRemove(targetConstraintId);
                    forceRemoveConstraint(serverLevel, targetConstraintId);
                    // config
                    if (VstuffConfig.ROPE_SOUNDS.get()) {
                        var style = (data != null) ? data.style.getBasicStyle() : RopeStyles.PrimitiveRopeStyle.NORMAL;

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

                    System.out.println("Removed constraint (1st attempt): " + targetConstraintId);

                    if (ConstraintTracker.getActiveConstraints().containsKey(targetConstraintId)) {
                        System.out.println("Constraint " + targetConstraintId + " still present, retrying...");

                        VSGameUtilsKt.getShipObjectWorld(serverLevel).removeConstraint(targetConstraintId);
                        ConstraintTracker.removeConstraintWithPersistence(serverLevel, targetConstraintId);
                        NetworkHandler.sendConstraintRemove(targetConstraintId);
                        forceRemoveConstraint(serverLevel, targetConstraintId);


                        System.out.println("Removed constraint (2nd attempt): " + targetConstraintId);
                    }

                    if (!player.getAbilities().instabuild) {
                        itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                        player.drop(new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get()), false);
                    }

                    return InteractionResultHolder.success(itemStack);
                } catch (Exception e) {
                    System.err.println("Error removing constraint: " + e.getMessage());
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


    private Integer findTargetedLead(ServerLevel level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        double maxDistance = 10.0;
        double minDistance = Double.MAX_VALUE;
        Integer closestConstraintId = null;

        for (Map.Entry<Integer, ConstraintTracker.RopeConstraintData> entry : ConstraintTracker.getActiveConstraints().entrySet()) {
            Integer constraintId = entry.getKey();
            ConstraintTracker.RopeConstraintData constraint = entry.getValue();

            Vector3d worldPosA = constraint.getWorldPosA(level, 1.0f);
            Vector3d worldPosB = constraint.getWorldPosB(level, 1.0f);

            double distance = getDistanceToRope(eyePos, lookVec, worldPosA, worldPosB, maxDistance);
            if (distance < minDistance && distance <= 1.0) {
                minDistance = distance;
                closestConstraintId = constraintId;
            }
        }

        return closestConstraintId;
    }

    private double getDistanceToRope(Vec3 eyePos, Vec3 lookVec, Vector3d ropeStart, Vector3d ropeEnd, double maxDistance) {
        Vec3 start = new Vec3(ropeStart.x, ropeStart.y, ropeStart.z);
        Vec3 end = new Vec3(ropeEnd.x, ropeEnd.y, ropeEnd.z);
        double minDistanceToRope = Double.MAX_VALUE;

        for (double t = 0; t <= maxDistance; t += 0.5) {
            Vec3 rayPoint = eyePos.add(lookVec.scale(t));
            Vec3 ropeVec = end.subtract(start);
            Vec3 startToRay = rayPoint.subtract(start);
            double ropeLength = ropeVec.length();
            if (ropeLength < 0.01) continue;

            double projection = startToRay.dot(ropeVec) / (ropeLength * ropeLength);
            projection = Math.max(0, Math.min(1, projection));

            Vec3 closestPointOnRope = start.add(ropeVec.scale(projection));
            double distanceToRope = rayPoint.distanceTo(closestPointOnRope);
            minDistanceToRope = Math.min(minDistanceToRope, distanceToRope);
        }

        return minDistanceToRope;
    }



}
