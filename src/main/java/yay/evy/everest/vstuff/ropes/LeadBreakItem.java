package yay.evy.everest.vstuff.ropes;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import yay.evy.everest.vstuff.item.ModItems;

import java.util.List;
import java.util.Map;

public class LeadBreakItem extends Item {

    public LeadBreakItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            Integer targetConstraintId = findTargetedLead(serverLevel, player);

            if (targetConstraintId != null) {

                try {
                    boolean removed = VSGameUtilsKt.getShipObjectWorld(serverLevel).removeConstraint(targetConstraintId);
                    if (removed) {
                        ConstraintTracker.removeConstraint(targetConstraintId);
                        player.sendSystemMessage(Component.literal("§aLead cut!"));

                        player.drop(new ItemStack(ModItems.LEAD_CONSTRAINT_ITEM.get()), false);

                        if (!player.getAbilities().instabuild) {
                            itemStack.shrink(1);
                        }

                        System.out.println("Cut constraint: " + targetConstraintId);
                        return InteractionResultHolder.success(itemStack);
                    } else {
                        player.sendSystemMessage(Component.literal("§cFailed to cut lead!"));
                        return InteractionResultHolder.fail(itemStack);
                    }
                } catch (Exception e) {
                    System.err.println("Error cutting constraint " + targetConstraintId + ": " + e.getMessage());
                    player.sendSystemMessage(Component.literal("§cError cutting lead: " + e.getMessage()));
                    return InteractionResultHolder.fail(itemStack);
                }

            } else {
                player.sendSystemMessage(Component.literal("§cNo lead found in your crosshair! Look directly at a lead to cut it."));
                return InteractionResultHolder.fail(itemStack);
            }
        }

        return InteractionResultHolder.pass(itemStack);
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


            Vector3d worldPosA = constraint.getWorldPosA(level, 1.0f); // Remove partialTick
            Vector3d worldPosB = constraint.getWorldPosB(level, 1.0f); // Remove partialTick


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

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Look at a lead and right-click to cut it"));
        tooltip.add(Component.literal("§7Drops a Ship Lead when used"));
        tooltip.add(Component.literal("§7Consumes the cutter when used"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
