package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
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
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeCategoryReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeRestyleReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeStyleReloadListener;
import yay.evy.everest.vstuff.internal.styling.RopeRestyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.HashSet;
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
}
