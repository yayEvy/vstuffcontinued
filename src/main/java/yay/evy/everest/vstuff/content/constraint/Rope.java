package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.valkyrienskies.core.apigame.constraints.VSRopeConstraint;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.RopeStyles;
import yay.evy.everest.vstuff.util.packet.RopeSoundPacket;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Rope {

    public static RopeUtil.RopeInteractionReturn createNew(LeadConstraintItem ropeItem, ServerLevel level, BlockPos firstClickedPos,
                                                           BlockPos secondClickedPos, Entity firstEntity, Long firstShipId,
                                                           Long secondShipId, Player player) {
        if (firstClickedPos == null && firstEntity == null) return RopeUtil.RopeInteractionReturn.FAIL;

        Vector3d firstWorldPos;
        Vector3d firstLocalPos;
        Long shipA;

        if (firstEntity != null) {
            firstWorldPos = new Vector3d(firstEntity.getX(), firstEntity.getY() + firstEntity.getBbHeight() / 2, firstEntity.getZ());
            shipA = firstShipId != null ? firstShipId : RopeUtil.getGroundBodyId(level);
            firstLocalPos = RopeUtil.convertWorldToLocal(level, firstWorldPos, shipA);
        } else {
            firstWorldPos = RopeUtil.getWorldPosition(level, firstClickedPos, firstShipId);
            shipA = firstShipId != null ? firstShipId : RopeUtil.getGroundBodyId(level);
            firstLocalPos = RopeUtil.getLocalPositionFixed(level, firstClickedPos, firstShipId, shipA);
        }

        Vector3d secondWorldPos = RopeUtil.getWorldPosition(level, secondClickedPos, secondShipId);
        Long shipB = secondShipId != null ? secondShipId : RopeUtil.getGroundBodyId(level);
        Vector3d secondLocalPos = RopeUtil.getLocalPositionFixed(level, secondClickedPos, secondShipId, shipB);

        return createNew(ropeItem, level, shipA, shipB, firstLocalPos, secondLocalPos, firstWorldPos, secondWorldPos, player);
    }

    public static RopeUtil.RopeInteractionReturn createNew(LeadConstraintItem ropeItem, ServerLevel level,
                                                           Long shipA, Long shipB,
                                                           Vector3d localPosA, Vector3d localPosB,
                                                           Vector3d worldPosA, Vector3d worldPosB,
                                                           Player player) {

        Long groundBodyId = RopeUtil.getGroundBodyId(level);
        Long finalShipA, finalShipB;
        Vector3d finalLocalPosA, finalLocalPosB;
        Vector3d finalWorldPosA, finalWorldPosB;

        boolean shipAIsWorld = shipA.equals(groundBodyId);
        boolean shipBIsWorld = shipB.equals(groundBodyId);

        if (shipAIsWorld && !shipBIsWorld) {
            finalShipA = shipB;
            finalShipB = shipA;
            finalLocalPosA = localPosB;
            finalLocalPosB = localPosA;
            finalWorldPosA = worldPosB;
            finalWorldPosB = worldPosA;
        } else {
            finalShipA = shipA;
            finalShipB = shipB;
            finalLocalPosA = localPosA;
            finalLocalPosB = localPosB;
            finalWorldPosA = worldPosA;
            finalWorldPosB = worldPosB;
        }

        double distance = finalWorldPosA.distance(finalWorldPosB);
        double maxAllowedLength = VstuffConfig.MAX_ROPE_LENGTH.get();

        if (distance > maxAllowedLength) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§cRope too long! Max length is " + maxAllowedLength + " blocks."),
                        true
                );
            }
            return RopeUtil.RopeInteractionReturn.FAIL;
        }

        double maxLength = distance + 0.5;
        double massA = RopeUtil.getMassForShip(level, finalShipA);
        double massB = RopeUtil.getMassForShip(level, finalShipB);
        double effectiveMass = Math.min(massA, massB);
        if (effectiveMass < 100.0) effectiveMass = 100.0;
        double compliance = 1e-12 / effectiveMass;
        double massRatio = Math.max(massA, massB) / Math.min(massA, massB);
        double baseMaxForce = 50000000000000.0;
        double maxForce = baseMaxForce * Math.min(massRatio, 20.0);

        if (shipAIsWorld || shipBIsWorld) {
            maxForce *= 10.0;
            compliance *= 0.05;
        }

        VSRopeConstraint ropeConstraint = new VSRopeConstraint(
                finalShipA, finalShipB,
                compliance,
                finalLocalPosA, finalLocalPosB,
                maxForce,
                maxLength
        );

        try {
            Integer constraintId = VSGameUtilsKt.getShipObjectWorld(level).createNewConstraint(ropeConstraint);
            if (constraintId != null) {
                ropeItem.setActiveConstraintId(constraintId);
                ConstraintTracker.addConstraintWithPersistence(level, constraintId, finalShipA, finalShipB,
                        finalLocalPosA, finalLocalPosB, maxLength,
                        compliance, maxForce, RopeStyleHandlerServer.getStyle(player.getUUID()));

                if (player instanceof ServerPlayer serverPlayer) {
                    RopeStyles.RopeStyle ropeStyle = RopeStyleHandlerServer.getStyle(player.getUUID());
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new RopeSoundPacket(false, ropeStyle.getBasicStyle())
                    );

                    ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);
                }

                if (player != null && !player.getAbilities().instabuild) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.getItem() instanceof LeadConstraintItem) {
                            stack.shrink(1);
                            break;
                        }
                    }
                }

                return RopeUtil.RopeInteractionReturn.SUCCESS;
            }
        } catch (Exception e) {
            VStuff.LOGGER.error("Error creating rope constraint: {}",  e.getMessage());
            e.printStackTrace();
        }

        return RopeUtil.RopeInteractionReturn.FAIL;

    }
}