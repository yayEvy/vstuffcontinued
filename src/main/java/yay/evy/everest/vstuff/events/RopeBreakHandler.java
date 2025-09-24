package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
        List<Integer> constraintsToRemove = new ArrayList<>();

        for (Map.Entry<Integer, RopeConstraintData> entry : ConstraintTracker.getActiveConstraints().entrySet()) {
            Integer id = entry.getKey();
            RopeConstraintData data = entry.getValue();

            if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY) continue;

            try {
                BlockPos dropPos = null;
                boolean remove = false;

                if (!data.isShipA) {
                    BlockPos posA = BlockPos.containing(data.localPosA.x, data.localPosA.y, data.localPosA.z);
                    if (brokenPos.equals(posA)) {
                        remove = true;
                        dropPos = posA;
                    }
                }
                if (!remove && !data.isShipB) {
                    BlockPos posB = BlockPos.containing(data.localPosB.x, data.localPosB.y, data.localPosB.z);
                    if (brokenPos.equals(posB)) {
                        remove = true;
                        dropPos = posB;
                    }
                }

                if (!remove && data.isShipA) {
                    BlockPos worldPosA = BlockPos.containing(
                            data.getWorldPosA(level, 0).x,
                            data.getWorldPosA(level, 0).y,
                            data.getWorldPosA(level, 0).z
                    );
                    if (brokenPos.equals(worldPosA)) {
                        remove = true;
                        dropPos = worldPosA;
                    }
                }

                if (!remove && data.isShipB) {
                    BlockPos worldPosB = BlockPos.containing(
                            data.getWorldPosB(level, 0).x,
                            data.getWorldPosB(level, 0).y,
                            data.getWorldPosB(level, 0).z
                    );
                    if (brokenPos.equals(worldPosB)) {
                        remove = true;
                        dropPos = worldPosB;
                    }
                }

                if (remove && dropPos != null) {
                    constraintsToRemove.add(id);

                    ItemStack ropeDrop = new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get());
                    ItemEntity itemEntity = new ItemEntity(
                            level,
                            dropPos.getX() + 0.5,
                            dropPos.getY() + 0.5,
                            dropPos.getZ() + 0.5,
                            ropeDrop
                    );
                    level.addFreshEntity(itemEntity);
                }

            } catch (Exception e) {
                System.err.println("[RopeBreakHandler] Failed checking constraint " + id + ": " + e.getMessage());
            }
        }

        for (Integer constraintId : constraintsToRemove) {
            try {
                ConstraintTracker.removeConstraintWithPersistence(level, constraintId);
            } catch (Exception e) {
                System.err.println("[RopeBreakHandler] Failed to remove constraint " + constraintId + ": " + e.getMessage());
            }
        }
    }
}
