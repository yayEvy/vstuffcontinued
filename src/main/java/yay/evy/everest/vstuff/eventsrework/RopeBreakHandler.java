package yay.evy.everest.vstuff.eventsrework;

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
import yay.evy.everest.vstuff.content.constraintrework.RopeManager;
import yay.evy.everest.vstuff.content.constraintrework.ropes.AbstractRope;
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

        for (Map.Entry<Integer, AbstractRope> entry : RopeManager.getActiveRopes().entrySet()) {
            Integer id = entry.getKey();
            AbstractRope rope = entry.getValue();


            try {
                BlockPos dropPos = null;
                boolean remove = false;

                if (!rope.ship0IsGround) {
                    BlockPos posA = BlockPos.containing(rope.localPos0.x, rope.localPos0.y, rope.localPos0.z);
                    if (brokenPos.equals(posA)) {
                        remove = true;
                        dropPos = posA;
                    }
                } else {
                    BlockPos worldPosA = BlockPos.containing(rope.worldPos0.x, rope.worldPos0.y, rope.worldPos0.z);
                    if (brokenPos.equals(worldPosA)) {
                        remove = true;
                        dropPos = worldPosA;
                    }
                }

                if (!remove) {
                    if (!rope.ship1IsGround) {
                        BlockPos posB = BlockPos.containing(rope.localPos1.x, rope.localPos1.y, rope.localPos1.z);
                        if (brokenPos.equals(posB)) {
                            remove = true;
                            dropPos = posB;
                        }
                    } else {
                        BlockPos worldPosB = BlockPos.containing(rope.worldPos1.x, rope.worldPos1.y, rope.worldPos1.z);
                        if (brokenPos.equals(worldPosB)) {
                            remove = true;
                            dropPos = worldPosB;
                        }
                    }
                }


                if (remove) {
                    RopeManager.REMOVE(level, id);

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
    }
}