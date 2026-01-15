package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.rope.roperework.Rope;
import yay.evy.everest.vstuff.content.rope.roperework.RopeUtil;
import yay.evy.everest.vstuff.content.rope.roperework.RopeManager;
import yay.evy.everest.vstuff.index.VStuffItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff")
public class RopeBreakHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        List<Integer> constraintsToRemove = new ArrayList<>();
        Map<Integer, Rope> toRemove = new HashMap<>();

        for (Map.Entry<Integer, Rope> entry : RopeManager.getActiveRopes().entrySet()) {
            Integer id = entry.getKey();
            Rope rope = entry.getValue();

            if (rope.posData0.blockType() == RopeUtil.BlockType.PULLEY) continue;

            try {
                BlockPos dropPos = null;
                boolean remove = false;

                if (!rope.posData0.isWorld()) {
                    Vector3f localPos0 = rope.posData0.localPos();
                    BlockPos posA = BlockPos.containing(localPos0.x, localPos0.y, localPos0.z);
                    if (brokenPos.equals(posA)) {
                        remove = true;
                        dropPos = posA;
                    }
                }
                if (!remove && !rope.posData1.isWorld()) {
                    Vector3f localPos1 = rope.posData1.localPos();
                    BlockPos posB = BlockPos.containing(localPos1.x, localPos1.y, localPos1.z);
                    if (brokenPos.equals(posB)) {
                        remove = true;
                        dropPos = posB;
                    }
                }

                if (!remove && rope.posData0.isWorld()) {
                    Vector3f worldPos0 = rope.posData0.getWorldPos();
                    BlockPos worldPosA = BlockPos.containing(worldPos0.x, worldPos0.y, worldPos0.z);
                    if (brokenPos.equals(worldPosA)) {
                        remove = true;
                        dropPos = worldPosA;
                    }
                }

                if (!remove && rope.posData1.isWorld()) {
                    Vector3f worldPos1 = rope.posData1.getWorldPos();
                    BlockPos blockPos1 = BlockPos.containing(worldPos1.x, worldPos1.y, worldPos1.z);
                    if (brokenPos.equals(blockPos1)) {
                        remove = true;
                        dropPos = blockPos1;
                    }
                }

                if (remove) {
                    toRemove.put(id, rope);

                    ItemStack ropeDrop = new ItemStack(VStuffItems.ROPE_ITEM.get());
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

        for (Map.Entry<Integer, Rope> ropeEntry: toRemove.entrySet()) {
            Integer id = ropeEntry.getKey();
            Rope rope = ropeEntry.getValue();
            try {
                rope.removeJoint(level);
            } catch (Exception e) {
                VStuff.LOGGER.error("[RopeBreakHandler] Failed to remove constraint {}: {}", id, e.getMessage());
            }
        }
    }
}