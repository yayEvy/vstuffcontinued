package yay.evy.everest.vstuff.events;

import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.stringtemplate.v4.ST;
import org.valkyrienskies.physics_api.constraints.ConstraintData;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.constraint.ConstraintPersistence;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.Map;

import static yay.evy.everest.vstuff.util.RopeStyles.PrimitiveRopeStyle.DYED;
import static yay.evy.everest.vstuff.util.RopeStyles.PrimitiveRopeStyle.WOOL;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID)
public class ColorHaggler {







    @SubscribeEvent
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        try {
            ItemStack itemStack = event.getItemStack();
            Item item = event.getItemStack().getItem();
            Level level = event.getLevel();
            Player player = event.getEntity();


            if (level instanceof ServerLevel serverLevel) {
                Integer targetConstraintId = findTargetedLead(serverLevel, player);
               // Integer newConstraintId = targetConstraintId + 1000000;
              //  String newPersistenceId = java.util.UUID.randomUUID().toString();
                if (player instanceof ServerPlayer serverPlayer) {
                    if (itemStack.is(Tags.Items.DYES)) {
                        if (!event.getLevel().isClientSide) {
                           /* System.out.println("ID: " + targetConstraintId);
                            System.out.println("Color: " + item);
                            System.out.println("Style: " + findRopesStyle(serverLevel, player));
                           System.out.println(ropeIsColorable(serverLevel,player));*/
                            if (ropeIsColorable(serverLevel, player)) {

                                String persistanceId = ConstraintTracker.persistanceIdViaConstraintId(targetConstraintId);
                                ConstraintTracker.RopeConstraintData data = ConstraintTracker.getActiveConstraints().get(targetConstraintId);
                                ConstraintTracker.RopeConstraintData newData = data;
                                ConstraintPersistence constraintPersistence = ConstraintPersistence.get(serverLevel);


                                if (findRopesStyle(serverLevel, player).equals("plain") || findRopesBasicStyle(serverLevel, player).equals("DYED")) {
                                    /*System.out.println("max length: " + data.maxLength);
                                    System.out.println("complience: " + data.compliance);
                                    System.out.println("max force: " + data.maxForce);
                                    System.out.println("constraint type: " + data.constraintType); */
                                    newData.style = new RopeStyles.RopeStyle(item.toString(), DYED, "vstuff.rope." + item.toString());

                                    ConstraintTracker.getActiveConstraints().put(targetConstraintId, newData);



                                    NetworkHandler.sendConstraintRerender( targetConstraintId, newData.shipA, newData.shipB
                                            , newData.localPosA, newData.localPosB, newData.maxLength, newData.style);
                                    constraintPersistence.addConstraint(persistanceId, newData.shipA, newData.shipB
                                            , newData.localPosA, newData.localPosB, newData.maxLength, newData.compliance, newData.maxForce, serverLevel, newData.constraintType, null, newData.style);
                                    ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);



                                   /* System.out.println("new max length: " + newData.maxLength);
                                    System.out.println("new complience: " + newData.compliance);
                                    System.out.println("new max force: " + newData.maxForce);
                                    System.out.println("new constraint type: " + newData.constraintType);*/
                                }

                                if (findRopesStyle(serverLevel, player).equals("white_wool") || findRopesBasicStyle(serverLevel, player).equals("WOOL")) {
                                  /*  System.out.println("max length: " + data.maxLength);
                                    System.out.println("complience: " + data.compliance);
                                    System.out.println("max force: " + data.maxForce);
                                    System.out.println("constraint type: " + data.constraintType);*/

                                    newData.style = new RopeStyles.RopeStyle(item.toString() + "_wool" , WOOL, "vstuff.rope." + item.toString() );

                                    //ConstraintTracker.getActiveConstraints().replace(targetConstraintId, data, newData);
                                    ConstraintTracker.getActiveConstraints().put(targetConstraintId, newData);

                                                    NetworkHandler.sendConstraintRerender( targetConstraintId, newData.shipA, newData.shipB
                                                          , newData.localPosA, newData.localPosB, newData.maxLength, newData.style);
                                    constraintPersistence.addConstraint(persistanceId, newData.shipA, newData.shipB
                                            , newData.localPosA, newData.localPosB, newData.maxLength, newData.compliance, newData.maxForce, serverLevel, newData.constraintType, null, newData.style);
                                                  ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);


                                  /*  System.out.println("new max length: " + newData.maxLength);
                                    System.out.println("new complience: " + newData.compliance);
                                    System.out.println("new max force: " + newData.maxForce);
                                    System.out.println("new constraint type: " + newData.constraintType);*/
                                }


                                ConstraintPersistence.get(serverLevel).saveNow(serverLevel);


                                itemStack.shrink(1);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
           /* System.err.println("Darn it, the colors arent coloring, I blame Wizard >:(");
            e.printStackTrace();*/}}










    private boolean ropeIsColorable(ServerLevel level, Player player){
        String ropesStyle = findRopesStyle(level, player);
        String basicStyle = findRopesBasicStyle(level, player);
        if (ropesStyle.equals("white_wool") || ropesStyle.equals("plain") ||basicStyle.equals("DYED") ||basicStyle.equals("WOOL")){
            return true;
        }


        return false;
    }

    private String findRopesStyle(ServerLevel level, Player player) {
        Integer targetConstraintId = findTargetedLead(level, player);
        String ropeStyle = null;
        ConstraintTracker.RopeConstraintData data = ConstraintTracker.getActiveConstraints().get(targetConstraintId);
        ropeStyle = data.style.getStyle();
        return ropeStyle;
    }

    private String findRopesBasicStyle(ServerLevel level, Player player) {

    Integer targetConstraintId = findTargetedLead(level, player);

        String basicRopeStyle = null;
        ConstraintTracker.RopeConstraintData wizardsWorstKeptSecret = ConstraintTracker.getActiveConstraints().get(targetConstraintId);
        basicRopeStyle = (wizardsWorstKeptSecret.style.getBasicStyle().toString());

        return basicRopeStyle;
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
