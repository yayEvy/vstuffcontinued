/*package yay.evy.everest.vstuff.events;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.content.constraint.LeadBreakItem;

import java.awt.event.ActionEvent;
import java.util.Map;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID)
public class ColorHaggler {




    @SubscribeEvent
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        try {
            ItemStack itemStack = event.getItemStack();
            System.out.println("test");


                if (itemStack.is(Tags.Items.DYES)) {
                    System.out.println("it works!");
                    if (!event.getLevel().isClientSide) {
                        System.out.println("real " + itemStack);


                    }
                }
            }

        catch (Exception e) {
            System.err.println("Darn it, the colors arent coloring, I blame Wizard >:(");
            e.printStackTrace();}}

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.RightClickItem event){
        if (!event.getLevel().isClientSide) {


        System.out.println("rope: " );
    }}

    private InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
        ItemStack itemStack = player.getItemInHand(hand);
if (level instanceof ServerLevel serverLevel) {
    Integer targetConstraintId = findTargetedLead(serverLevel, player);

    System.out.println("rope: " + targetConstraintId);
}

        return InteractionResultHolder.success(itemStack);
    }

    private static Integer findTargetedLead(ServerLevel level, Player player) {
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
    private static double getDistanceToRope(Vec3 eyePos, Vec3 lookVec, Vector3d ropeStart, Vector3d ropeEnd, double maxDistance) {
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
*/