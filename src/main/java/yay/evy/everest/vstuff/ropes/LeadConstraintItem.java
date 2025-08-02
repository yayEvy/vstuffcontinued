package yay.evy.everest.vstuff.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.apigame.constraints.VSRopeConstraint;

public class LeadConstraintItem extends Item {
    private BlockPos firstClickedPos;
    private Long firstShipId;
    private Entity firstEntity;
    private Integer activeConstraintId;

    public LeadConstraintItem() {
        super(new Properties().stacksTo(64));
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos().immutable();
        Player player = context.getPlayer();

        if (level instanceof ServerLevel serverLevel) {
            System.out.println("LeadConstraintItem.useOn - BlockPos: " + blockPos + ", Block: " + level.getBlockState(blockPos).getBlock());

            Long shipId = getShipIdAtPos(serverLevel, blockPos);
            if (firstClickedPos == null && firstEntity == null) {
                firstClickedPos = blockPos;
                firstShipId = shipId;
                return InteractionResult.SUCCESS;
            } else {
                if (firstClickedPos != null && firstClickedPos.equals(blockPos)) {
                    resetState();
                    return InteractionResult.FAIL;
                }
                createLeadConstraint(serverLevel, blockPos, shipId, player);
                // Add sync after creating constraint
                if (player instanceof ServerPlayer serverPlayer) {
                    ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);
                }
                resetState();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }




    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.literal("§7Right-click two blocks to create rope constraint"));
        tooltip.add(Component.literal("§7Persists between world/server restarts"));
        tooltip.add(Component.literal("§7Works between ships and the world"));
        super.appendHoverText(stack, level, tooltip, flag);
    }




    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, net.minecraft.world.InteractionHand hand) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            Long shipId = getShipIdAtPos(serverLevel, entity.blockPosition());

            if (firstClickedPos == null && firstEntity == null) {
                firstEntity = entity;
                firstShipId = shipId;
                return InteractionResult.SUCCESS;
            } else {
                if (firstEntity != null && firstEntity.equals(entity)) {
                    //  player.sendSystemMessage(Component.literal("§cCannot connect a lead to the same entity!"));
                    return InteractionResult.FAIL;
                }
                createLeadConstraintToEntity(serverLevel, entity, shipId, player);
                resetState();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }


    private void createLeadConstraint(ServerLevel level, BlockPos secondPos, Long secondShipId, Player player) {
        if (firstClickedPos == null && firstEntity == null) return;

        /*
        // Double-check that neither position is a pulley block
        if (level.getBlockState(secondPos).getBlock() instanceof RopePulleyBlock) {
            player.sendSystemMessage(Component.literal("§cCannot create rope constraint to pulley block!"));
            return;
        }

         */

        /*
        if (firstClickedPos != null && level.getBlockState(firstClickedPos).getBlock() instanceof RopePulleyBlock) {
            player.sendSystemMessage(Component.literal("§cCannot create rope constraint from pulley block!"));
            return;
        }

         */

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

    private void createLeadConstraintToEntity(ServerLevel level, Entity secondEntity, Long secondShipId, Player player) {
        if (firstClickedPos == null && firstEntity == null) return;

        /*
        // Check if first position was a pulley block
        if (firstClickedPos != null && level.getBlockState(firstClickedPos).getBlock() instanceof RopePulleyBlock) {
            player.sendSystemMessage(Component.literal("§cCannot create rope constraint from pulley block!"));
            return;
        }

         */

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

        Vector3d secondWorldPos = new Vector3d(secondEntity.getX(), secondEntity.getY() + secondEntity.getBbHeight() / 2, secondEntity.getZ());
        Long shipB = secondShipId != null ? secondShipId : getGroundBodyId(level);
        Vector3d secondLocalPos = convertWorldToLocal(level, secondWorldPos, shipB);

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
                        compliance, maxForce);

                //  player.sendSystemMessage(Component.literal("§aRope constraint created! Length: " + String.format("%.1f", maxLength)));

                if (player != null) {
                    if (!player.getAbilities().instabuild) {
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (stack.getItem() instanceof LeadConstraintItem) {
                                stack.shrink(1);
                                break;
                            }
                        }
                    }
                }
            } else {
                //   player.sendSystemMessage(Component.literal("§cFailed to create rope constraint!"));
            }
        } catch (Exception e) {
            System.err.println("Error creating rope constraint: " + e.getMessage());
            //   player.sendSystemMessage(Component.literal("§cError creating rope constraint: " + e.getMessage()));
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

    // Make sure resetState is properly implemented
    private void resetState() {
        firstClickedPos = null;
        firstShipId = null;
        firstEntity = null;
        System.out.println("LeadConstraintItem state reset");
    }


    public void breakLead(Player player) {
        if (activeConstraintId != null && player.level() instanceof ServerLevel serverLevel) {
            try {
                boolean removed = VSGameUtilsKt.getShipObjectWorld(serverLevel).removeConstraint(activeConstraintId);
                if (removed) {
                    ConstraintTracker.removeConstraintWithPersistence(serverLevel, activeConstraintId);
                    player.sendSystemMessage(Component.literal("Lead broken!"));
                    System.out.println("Removed constraint: " + activeConstraintId);
                } else {
                    player.sendSystemMessage(Component.literal("Failed to break lead - constraint not found!"));
                }
                activeConstraintId = null;
            } catch (Exception e) {
                System.err.println("Error removing constraint: " + e.getMessage());
                player.sendSystemMessage(Component.literal("Error breaking lead: " + e.getMessage()));
            }
        } else {
            player.sendSystemMessage(Component.literal("No active lead to break!"));
        }
    }


}
