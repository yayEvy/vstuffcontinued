package yay.evy.everest.vstuff.eventsrework;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.constraintrework.MasterOfRopes;
import yay.evy.everest.vstuff.content.constraintrework.items.RopeItem;
import yay.evy.everest.vstuff.content.constraintrework.ropes.AbstractRope;
import yay.evy.everest.vstuff.content.constraintrework.ropes.RopeUtils;
import yay.evy.everest.vstuff.index.VStuffItems;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff")
public class RopeBreakHandler {

    static Map<BlockPos, ItemStack> ropeItems = new HashMap<>();

    public static void addRopeItemTo(ItemStack ropeItem) {
        ropeItems.put(NbtUtils.readBlockPos(ropeItem.getTag().getCompound("pos")), ropeItem);
    }

    public static void removeRopeItem(BlockPos pos) {
        ropeItems.remove(pos);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();

        for (Map.Entry<BlockPos, ItemStack> ropeItem : ropeItems.entrySet()) {
            if (ropeItem.getKey() == brokenPos) {
                RopeItem rope = (RopeItem) ropeItem.getValue().getItem();
                rope.reset(ropeItem.getValue());
            }
        }

        for (Map.Entry<Integer, AbstractRope> entry : MasterOfRopes.getAllActiveRopes().entrySet()) {
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
                    Vector3d worldPos0 = RopeUtils.convertLocalToWorld(level, rope.localPos0, rope.ship0);
                    BlockPos worldPosA = BlockPos.containing(worldPos0.x, worldPos0.y, worldPos0.z);
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
                        Vector3d worldPos1 = RopeUtils.convertLocalToWorld(level, rope.localPos0, rope.ship0);
                        BlockPos worldPosB = BlockPos.containing(worldPos1.x, worldPos1.y, worldPos1.z);
                        if (brokenPos.equals(worldPosB)) {
                            remove = true;
                            dropPos = worldPosB;
                        }
                    }
                }


                if (remove) {
                    MasterOfRopes.REMOVE(level, id);

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
    }
}