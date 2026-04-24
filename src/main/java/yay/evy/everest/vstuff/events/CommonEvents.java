package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
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
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.content.ropes.packet.SyncRopeCategoriesPacket;
import yay.evy.everest.vstuff.content.ropes.packet.SyncRopeRestylesPacket;
import yay.evy.everest.vstuff.content.ropes.packet.SyncRopeStylesPacket;
import yay.evy.everest.vstuff.content.ropes.phys_ropes.PhysRopeConstraint;
import yay.evy.everest.vstuff.content.ropes.phys_ropes.PhysRopeManager;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeCategoryReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeRestyleReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeStyleReloadListener;
import yay.evy.everest.vstuff.internal.styling.RopeRestyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RopeStyleReloadListener());
        event.addListener(new RopeCategoryReloadListener());
        event.addListener(new RopeRestyleReloadListener());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        Vector3d worldBreakPos = RopeUtils.getWorldPos(level, brokenPos);
        Map<Integer, ResourceLocation> idsToRemove = new HashMap<>();

        for (ReworkedRope rope : RopeManager.get(level).getRopeList())
            if (rope.atBlockPos(brokenPos)) {
                idsToRemove.put(rope.getRopeId(), rope.style.id());
            }

        for (Map.Entry<Integer, ResourceLocation> entry : idsToRemove.entrySet()) {
            ItemStack ropeStack = new ItemStack(VStuffItems.ROPE.get());
            ropeStack.getOrCreateTag().put("style", TagUtils.writeResourceLocation(entry.getValue()));

            ItemEntity droppedEntity = new ItemEntity(
                    level,
                    worldBreakPos.x,
                    worldBreakPos.y,
                    worldBreakPos.z,
                    ropeStack
            );

            level.addFreshEntity(droppedEntity);

            RopeFactory.removeRope(level, entry.getKey());
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        RopeManager.syncAllRopesToPlayer(player);
        // sync phys ropes
        PhysRopeManager.get(level).syncAllToPlayer(level, player);
        // wisconsin
        VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new SyncRopeStylesPacket());
        VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new SyncRopeCategoriesPacket());
        VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new SyncRopeRestylesPacket());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) { // fired when right-clicking air // i see
        handleRightClickEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) { // fired when right-clicking a block // oh ok
        handleRightClickEvent(event);
    }

    private static void handleRightClickEvent(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (RopeRestyleManager.isValidRetyping(itemStack.getItem())) {
            if ((event.getLevel() instanceof ServerLevel level)) {
                Player player = event.getEntity();
                ReworkedRope rope = RopeUtils.findRope(level, player);
                if (rope == null) return;

                if (RopeRestyleManager.canRetype(rope.style, itemStack.getItem())) {
                    RopeStyle newType = RopeRestyleManager.retype(rope.style, itemStack.getItem());
                    if (newType == null) {
                        return;
                    }
                    RopeFactory.retypeRope(level, rope.getRopeId(), newType.id());

                    RopeUtils.playSound(level, rope.posData0.blockPos(), rope.style.placeSound());
                    RopeUtils.playSound(level, rope.posData1.blockPos(), rope.style.breakSound());

                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
    }

    // guh
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();

        RopeManager.syncAllRopesToPlayer(player);

        // sink them here too
        PhysRopeManager.get(level).syncAllToPlayer(level, player);
        // hell michigan

        VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new SyncRopeStylesPacket());
        VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new SyncRopeCategoriesPacket());
        VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new SyncRopeRestylesPacket());
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            PhysRopeManager.get(level).tickSegmentSync(level);
        }
    }
    /* yeet
    // todo yeet these well not yeet but like not do it so sussy
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();

        PhysRopeManager manager = PhysRopeManager.get(level);

        for (Map.Entry<Integer, PhysRopeConstraint> entry : manager.getPhysRopes().entrySet()) {
            PhysRopeConstraint c = entry.getValue();
            c.recreatePhysEntities(level);
            c.posData0.attach(level, entry.getKey());
            c.posData1.attach(level, entry.getKey());
        }

        manager.setDirty();

        for (PhysRopeConstraint c : manager.getPhysRopes().values()) {
            c.restoreJoints(level, c.getSegments(), c.getSegmentLength());
        }
    }

     */
    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.equals(level.getServer().overworld())) return;
        PhysRopeManager.get(level).setDirty();
    }
}
