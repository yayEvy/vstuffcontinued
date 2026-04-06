package yay.evy.everest.vstuff.events;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.*;
import yay.evy.everest.vstuff.content.ropes.type.RopeType;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.infrastructure.data.RopeCategoryReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.RopeRestyleReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.RopeTypeReloadListener;
import yay.evy.everest.vstuff.internal.RopeRestyleManager;
import yay.evy.everest.vstuff.internal.utility.GTPAUtils;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RopeTypeReloadListener());
        event.addListener(new RopeCategoryReloadListener());
        event.addListener(new RopeRestyleReloadListener());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        Vector3d worldBreakPos = RopeUtils.getWorldPos(level, brokenPos);
        Set<Integer> idsToRemove = new HashSet<>();

        for (ReworkedRope rope : RopeManager.get(level).getRopeList())
            if (rope.atBlockPos(brokenPos)) {
                idsToRemove.add(rope.getRopeId());
            }

        for (Integer id : idsToRemove) {
            ItemStack drop = new ItemStack(VStuffItems.ROPE.get());
            ItemEntity droppedEntity = new ItemEntity(
                    level,
                    worldBreakPos.x,
                    worldBreakPos.y,
                    worldBreakPos.z,
                    drop
            );

            level.addFreshEntity(droppedEntity);

            RopeFactory.removeRope(level, id);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        RopeManager.syncAllRopesToPlayer(player);
        PhysRopeManager.get(player.serverLevel()).syncAllToPlayer(player.serverLevel(), player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().getItem() instanceof BlockItem) return;
        handleRightClickEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleRightClickEvent(event);
    }

    private static void handleRightClickEvent(PlayerInteractEvent event) {
        if ((event.getLevel() instanceof ClientLevel level)) {
            if (RopeUtils.findTargetedLeadClient(level, event.getEntity()) != null) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        } else if ((event.getLevel() instanceof ServerLevel level)) {
            ItemStack itemStack = event.getItemStack();
            Player player = event.getEntity();

            ReworkedRope rope = RopeUtils.findTargetedLead(level, player);
            if (rope == null) return;

            RopeType newType = RopeRestyleManager.retype(rope.type, itemStack.getItem());
            if (!newType.equals(rope.type)) {
                RopeFactory.retypeRope(level, rope.getRopeId(), newType.id());
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ServerLevel level = event.getServer().overworld();
        PhysRopeManager.get(level).tickSegmentSync(level);
    }


    // todo yeet these well not yeet but like not do it so sussy
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + 40,
                () -> {
                    PhysRopeManager manager = PhysRopeManager.get(level);
                    for (Map.Entry<Integer, PhysRopeConstraint> entry : manager.getPhysRopes().entrySet()) {
                        PhysRopeConstraint c = entry.getValue();
                        c.recreatePhysEntities(level);
                        c.posData0.attach(level, entry.getKey());
                        c.posData1.attach(level, entry.getKey());
                        manager.setDirty();
                    }
                    level.getServer().tell(new net.minecraft.server.TickTask(
                            level.getServer().getTickCount() + 60,
                            () -> {
                                for (PhysRopeConstraint c : manager.getPhysRopes().values()) {
                                    c.restoreJoints(level, c.getSegments(), c.getSegmentLength());
                                }
                            }
                    ));
                }
        ));
    }
    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.equals(level.getServer().overworld())) return;

        PhysRopeManager manager = PhysRopeManager.get(level);
        for (PhysRopeConstraint c : manager.getPhysRopes().values()) {
            if (c.hasSegments()) {
                c.clearJointIds(level);
                c.restoreJoints(level, c.getSegments(), c.getSegmentLength());
            }
        }
    }
}
