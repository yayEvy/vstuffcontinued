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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.internal.RopeType;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeCategoryReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeRestyleReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeTypeReloadListener;
import yay.evy.everest.vstuff.internal.RopeRestyleManager;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.HashSet;
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
            if (event.getItemStack().is(VStuffItems.ROPE_CUTTER.get())) {
                return;
            }
            ItemStack itemStack = event.getItemStack();
            Player player = event.getEntity();

            ReworkedRope rope = RopeUtils.findTargetedLead(level, player);
            if (rope == null) return;

            if (!RopeRestyleManager.canRetyle(rope.type, itemStack.getItem())) {
                return;
            }

            RopeType newType = RopeRestyleManager.retype(rope.type, itemStack.getItem());
            if (!newType.equals(rope.type)) {
                RopeFactory.retypeRope(level, rope.getRopeId(), newType.id());
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}
