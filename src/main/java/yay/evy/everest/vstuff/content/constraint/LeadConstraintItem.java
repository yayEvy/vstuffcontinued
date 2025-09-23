package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.apigame.constraints.VSRopeConstraint;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyItem;
import yay.evy.everest.vstuff.network.RopeSoundPacket;
import yay.evy.everest.vstuff.sound.RopeSoundHandler;
import net.minecraft.sounds.SoundEvents;
import yay.evy.everest.vstuff.utils.RopeStyles;

public class LeadConstraintItem extends Item {
    private BlockPos firstClickedPos;
    private Long firstShipId;
    private Entity firstEntity;
    private Integer activeConstraintId;
    private ResourceKey<Level> firstClickDimension;
    private String ropeStyle = "normal";
    private RopeStyles.RopeStyle style = new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.BASIC, "vstuff.ropes.normal");

    public LeadConstraintItem(Properties pProperties) {
        super(new Properties().stacksTo(64));
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos().immutable();
        Player player = context.getPlayer();

        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.PASS;
        }

        // Handle pulley targeting first
        PhysPulleyBlockEntity pulley = PhysPulleyItem.getWaitingPulley(player);
        if (pulley != null) {
            pulley.targetPos = clickedPos;
            pulley.hasTarget = true;
            pulley.waitingForTarget = false;
            pulley.setChanged();
            pulley.sendData();

            pulley.createManualConstraint();

            player.sendSystemMessage(Component.literal("§aPulley target set and constraint created!"));
            PhysPulleyItem.clearWaitingPulley(player);

            return InteractionResult.SUCCESS;
        }

        // Otherwise, handle rope constraint logic
        if (firstClickedPos == null && firstEntity == null) {
            firstClickedPos = clickedPos;
            firstShipId = getShipIdAtPos(serverLevel, clickedPos);
            firstClickDimension = serverLevel.dimension();
            return InteractionResult.SUCCESS;
        } else {
            // Prevent double-click on same block
            if (firstClickedPos != null && firstClickedPos.equals(clickedPos)) {
                resetState();
                return InteractionResult.FAIL;
            }

            // Dimension safety check
            if (!serverLevel.dimension().equals(firstClickDimension)) {
                player.displayClientMessage(
                        Component.literal("§cCannot create rope across dimensions!"),
                        true
                );
                resetState();
                return InteractionResult.FAIL;
            }

            Long secondShipId = getShipIdAtPos(serverLevel, clickedPos);
            createLeadConstraint(serverLevel, clickedPos, secondShipId, player);

            if (player instanceof ServerPlayer serverPlayer) {
                ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);
            }

            resetState();
            return InteractionResult.SUCCESS;
        }
    }

    private void createLeadConstraint(ServerLevel level, BlockPos secondPos, Long secondShipId, Player player) {
        if (firstClickedPos == null && firstEntity == null) return;

        Vector3d firstWorldPos;
        Vector3d firstLocalPos;
        Long shipA;

        if (firstEntity != null) {
            firstWorldPos = new Vector3d(firstEntity.getX(), firstEntity.getY() + firstEntity.getBbHeight() / 2, firstEntity.getZ());
            shipA = firstShipId != null ? firstShipId : getGroundBodyId(level);
            firstLocalPos = convertWorldToLocal(level, firstWorldPos, shipA);
        } else {
            firstWorldPos = getWorldPosition(level, firstClickedPos, firstShipId);
            shipA = firstShipId != null ? firstShipId : getGroundBodyId(level);
            firstLocalPos = getLocalPositionFixed(level, firstClickedPos, firstShipId, shipA);
        }

        Vector3d secondWorldPos = getWorldPosition(level, secondPos, secondShipId);
        Long shipB = secondShipId != null ? secondShipId : getGroundBodyId(level);
        Vector3d secondLocalPos = getLocalPositionFixed(level, secondPos, secondShipId, shipB);

        createConstraintConsistent(level, shipA, shipB, firstLocalPos, secondLocalPos, firstWorldPos, secondWorldPos, player);
    }

    private void createConstraintConsistent(ServerLevel level, Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                            Vector3d worldPosA, Vector3d worldPosB, Player player) {
        Long groundBodyId = getGroundBodyId(level);
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
        double maxLength = distance + 0.5;
        double massA = getMassForShip(level, finalShipA);
        double massB = getMassForShip(level, finalShipB);
        double effectiveMass = Math.min(massA, massB);
        if (effectiveMass < 100.0) effectiveMass = 100.0;
        double compliance = 1e-12 / effectiveMass;
        double massRatio = Math.max(massA, massB) / Math.min(massA, massB);
        double baseMaxForce = 50000000000000.0;
        double maxForce = baseMaxForce * Math.min(massRatio, 20.0);


        double maxAllowedLength = VstuffConfig.MAX_ROPE_LENGTH.get();

        if (distance > maxAllowedLength) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§cRope too long! Max length is " + maxAllowedLength + " blocks."),
                        true
                );
            }
            resetState();
            return;
        }


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
                activeConstraintId = constraintId;
                ConstraintTracker.addConstraintWithPersistence(level, constraintId, finalShipA, finalShipB,
                        finalLocalPosA, finalLocalPosB, maxLength,
                        compliance, maxForce, style);

                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new RopeSoundPacket(false)
                    );
// config
                    if (VstuffConfig.ROPE_SOUNDS.get()) {
                        level.playSound(
                                null,
                                BlockPos.containing(finalWorldPosA.x, finalWorldPosA.y, finalWorldPosA.z),
                                SoundEvents.LEASH_KNOT_PLACE,
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F
                        );
                    }

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
            }
        } catch (Exception e) {
            System.err.println("Error creating rope constraint: " + e.getMessage());
            e.printStackTrace();
        }

    }
    private double getMassForShip(ServerLevel level, Long shipId) {
        Long groundBodyId = getGroundBodyId(level);
        if (shipId.equals(groundBodyId)) {
            return 1e12;
        }

        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
        if (shipObject != null) {
            try {
                double mass = 1000.0;
                var bounds = shipObject.getShipAABB();
                if (bounds != null) {
                    double volume = (bounds.maxX() - bounds.minX()) *
                            (bounds.maxY() - bounds.minY()) *
                            (bounds.maxZ() - bounds.minZ());
                    mass = Math.max(volume * 10.0, 1000.0);
                }
                return Math.min(mass, 1e9);
            } catch (Exception e) {
                return 1000.0;
            }
        }
        return 1000.0;
    }

    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        Ship shipObject = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        return shipObject != null ? shipObject.getId() : null;
    }

    private Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
    }

    private Vector3d getWorldPosition(ServerLevel level, BlockPos pos, Long shipId) {
        Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (shipId != null) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d worldPos = new Vector3d();
                shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                return worldPos;
            }
        }
        return localPos;
    }

    private Vector3d getLocalPositionFixed(ServerLevel level, BlockPos pos, Long clickedShipId, Long targetShipId) {
        Vector3d blockPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (clickedShipId != null && clickedShipId.equals(targetShipId)) {
            return blockPos;
        }

        if (targetShipId.equals(getGroundBodyId(level))) {
            if (clickedShipId != null) {
                Ship clickedShip = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(clickedShipId);
                if (clickedShip != null) {
                    Vector3d worldPos = new Vector3d();
                    clickedShip.getTransform().getShipToWorld().transformPosition(blockPos, worldPos);
                    return worldPos;
                }
            }
            return blockPos;
        }

        Ship targetShip = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(targetShipId);
        if (targetShip != null) {
            Vector3d worldPos = blockPos;
            if (clickedShipId != null && !clickedShipId.equals(targetShipId)) {
                Ship clickedShip = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(clickedShipId);
                if (clickedShip != null) {
                    worldPos = new Vector3d();
                    clickedShip.getTransform().getShipToWorld().transformPosition(blockPos, worldPos);
                }
            }
            Vector3d localPos = new Vector3d();
            targetShip.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
            return localPos;
        }
        return blockPos;
    }

    private Vector3d convertWorldToLocal(ServerLevel level, Vector3d worldPos, Long shipId) {
        if (shipId != null && !shipId.equals(getGroundBodyId(level))) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d localPos = new Vector3d();
                shipObject.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
                return localPos;
            }
        }
        return new Vector3d(worldPos);
    }

    private void resetState() {
        firstClickedPos = null;
        firstShipId = null;
        firstEntity = null;
        firstClickDimension = null;
        System.out.println("LeadConstraintItem state reset");
    }


}