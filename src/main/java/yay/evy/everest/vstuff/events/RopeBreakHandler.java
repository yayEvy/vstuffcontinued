package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker.RopeConstraintData;
import yay.evy.everest.vstuff.index.VStuffItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff")
public class RopeBreakHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        System.out.println("[RopeBreakHandler] Block broken at " + brokenPos + " in " + level.dimension().location());

        List<Integer> constraintsToRemove = new ArrayList<>();

        for (Map.Entry<Integer, RopeConstraintData> entry : ConstraintTracker.getActiveConstraints().entrySet()) {
            Integer id = entry.getKey();
            RopeConstraintData data = entry.getValue();

            try {

                boolean matches = false;

                if (data.sourceBlockPos != null && data.sourceBlockPos.equals(brokenPos)) {
                    matches = true;
                    System.out.println("[RopeBreakHandler] Match: sourceBlockPos equals brokenPos for constraint " + id);
                }

                if (!matches) {
                    Vector3d worldA = data.getWorldPosA(level, 0.0f);
                    BlockPos worldBlockA = new BlockPos(
                            (int) Math.floor(worldA.x),
                            (int) Math.floor(worldA.y),
                            (int) Math.floor(worldA.z)
                    );
                    if (worldBlockA.equals(brokenPos)) {
                        matches = true;
                        System.out.println("[RopeBreakHandler] Match: anchor A equals brokenPos for constraint " + id);
                    }
                }

                if (!matches) {
                    Vector3d worldB = data.getWorldPosB(level, 0.0f);
                    BlockPos worldBlockB = new BlockPos(
                            (int) Math.floor(worldB.x),
                            (int) Math.floor(worldB.y),
                            (int) Math.floor(worldB.z)
                    );
                    if (worldBlockB.equals(brokenPos)) {
                        matches = true;
                        System.out.println("[RopeBreakHandler] Match: anchor B equals brokenPos for constraint " + id);
                    }
                }

                if (matches) {
                    constraintsToRemove.add(id);
                }

            } catch (Exception ex) {
                System.err.println("[RopeBreakHandler] Error while checking constraint " + id + ": " + ex.getMessage());
            }
        }

        for (Integer constraintId : constraintsToRemove) {
            try {
                System.out.println("[RopeBreakHandler] Removing constraint " + constraintId + " due to block break at " + brokenPos);
                ConstraintTracker.removeConstraintWithPersistence(level, constraintId);
                ConstraintTracker.cleanupOrphanedConstraints(level, brokenPos);

                ItemStack ropeDrop = new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get());
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        brokenPos.getX() + 0.5,
                        brokenPos.getY() + 0.5,
                        brokenPos.getZ() + 0.5,
                        ropeDrop
                );
                level.addFreshEntity(itemEntity);
            } catch (Exception e) {
                System.err.println("[RopeBreakHandler] Failed to remove/drop for constraint " + constraintId + ": " + e.getMessage());
            }
        }
    }
}
