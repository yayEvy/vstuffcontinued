package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.content.constraint.Rope;
import yay.evy.everest.vstuff.content.constraint.RopeUtil;
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

        for (Map.Entry<Integer, Rope> entry : ConstraintTracker.getActiveRopes().entrySet()) {
            Integer id = entry.getKey();
            Rope rope = entry.getValue();

            if (rope.constraintType == RopeUtil.ConstraintType.PULLEY) continue;

            try {
                BlockPos dropPos = null;
                boolean remove = false;

                if (!rope.shipAIsGround) {
                    BlockPos posA = BlockPos.containing(rope.localPosA.x, rope.localPosA.y, rope.localPosA.z);
                    if (brokenPos.equals(posA)) {
                        remove = true;
                        dropPos = posA;
                    }
                }
                if (!remove && !rope.shipBIsGround) {
                    BlockPos posB = BlockPos.containing(rope.localPosB.x, rope.localPosB.y, rope.localPosB.z);
                    if (brokenPos.equals(posB)) {
                        remove = true;
                        dropPos = posB;
                    }
                }

                if (!remove && rope.shipAIsGround) {
                    BlockPos worldPosA = BlockPos.containing(
                            rope.getWorldPosA(level).x,
                            rope.getWorldPosA(level).y,
                            rope.getWorldPosA(level).z
                    );
                    if (brokenPos.equals(worldPosA)) {
                        remove = true;
                        dropPos = worldPosA;
                    }
                }

                if (!remove && rope.shipBIsGround) {
                    BlockPos worldPosB = BlockPos.containing(
                            rope.getWorldPosB(level).x,
                            rope.getWorldPosB(level).y,
                            rope.getWorldPosB(level).z
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