package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
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

            if (data.constraintType == ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY) {
                continue;
            }

            try {
                boolean validA = ConstraintTracker.isValidAttachmentPoint(
                        level,
                        data.localPosA,
                        data.shipA,
                        VSGameUtilsKt.getShipObjectWorld(level)
                                .getDimensionToGroundBodyIdImmutable()
                                .get(VSGameUtilsKt.getDimensionId(level)),
                        data.isShipA
                );

                boolean validB = ConstraintTracker.isValidAttachmentPoint(
                        level,
                        data.localPosB,
                        data.shipB,
                        VSGameUtilsKt.getShipObjectWorld(level)
                                .getDimensionToGroundBodyIdImmutable()
                                .get(VSGameUtilsKt.getDimensionId(level)),
                        data.isShipB
                );

                if (!validA || !validB) {
                    constraintsToRemove.add(id);
                }
            } catch (Exception e) {
                System.err.println("[RopeBreakHandler] Error validating constraint " + id + ": " + e.getMessage());
            }
        }

        for (Integer constraintId : constraintsToRemove) {
            try {
                ConstraintTracker.removeConstraintWithPersistence(level, constraintId);

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