package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static yay.evy.everest.vstuff.internal.utility.RopeUtils.containingBlockPos;

@Mod.EventBusSubscriber(modid = "vstuff")
public class RopeBreakHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        List<Integer> constraintsToRemove = new ArrayList<>();
        Map<Integer, ReworkedRope> toRemove = new HashMap<>();

        for (Map.Entry<Integer, ReworkedRope> entry : RopeManager.getActiveRopes().entrySet()) {
            Integer id = entry.getKey();
            ReworkedRope rope = entry.getValue();

            try {
                BlockPos dropPos = null;
                boolean remove = false;

                if (!rope.posData0.isWorld()) {
                    BlockPos posA = containingBlockPos(rope.posData0.localPos());
                    if (brokenPos.equals(posA)) {
                        remove = true;
                        dropPos = posA;
                    }
                }
                if (!remove && !rope.posData1.isWorld()) {
                    BlockPos posB = containingBlockPos(rope.posData1.localPos());
                    if (brokenPos.equals(posB)) {
                        remove = true;
                        dropPos = posB;
                    }
                }

                if (!remove && rope.posData0.isWorld()) {
                    BlockPos worldPosA = containingBlockPos(rope.posData0.getWorldPos(level));
                    if (brokenPos.equals(worldPosA)) {
                        remove = true;
                        dropPos = worldPosA;
                    }
                }

                if (!remove && rope.posData1.isWorld()) {
                    BlockPos worldPosB = containingBlockPos(rope.posData1.getWorldPos(level));
                    if (brokenPos.equals(worldPosB)) {
                        remove = true;
                        dropPos = worldPosB;
                    }
                }

                if (remove) {
                    toRemove.put(id, rope);

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
                VStuff.LOGGER.error("[RopeBreakHandler] Failed checking constraint {}: {}", id, e.getMessage());
            }
        }

        for (Map.Entry<Integer, ReworkedRope> ropeEntry: toRemove.entrySet()) {
            Integer id = ropeEntry.getKey();
            ReworkedRope rope = ropeEntry.getValue();
            try {
                rope.removeJoint(level);
            } catch (Exception e) {
                VStuff.LOGGER.error("[RopeBreakHandler] Failed to remove constraint {}: {}", id, e.getMessage());
            }
        }
    }
}