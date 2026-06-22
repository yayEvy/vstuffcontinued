package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.infrastructure.commands.VStuffCommands;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeCategoryReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeRestyleReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.listener.RopeStyleReloadListener;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event){
        VStuffCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        Vector3d worldBreakPos = RopeUtils.getWorldPos(level, brokenPos);
        Map<Integer, ResourceKey<RopeStyle>> idsToRemove = new HashMap<>();

        for (ReworkedRope rope : RopeManager.get(level).getRopeList())
            if (rope.atBlockPos(brokenPos)) {
                idsToRemove.put(rope.getRopeId(), rope.styleKey);
            }

        for (Map.Entry<Integer, ResourceKey<RopeStyle>> entry : idsToRemove.entrySet()) {
            ItemStack ropeStack = new ItemStack(VStuffItems.ROPE.get());
            ropeStack.getOrCreateTag().put("style", TagUtils.writeResourceKey(entry.getValue()));

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
        RopeManager.syncAllRopesToPlayer(player);
    }

// todo reimplement restyling n stuff

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) { // fired when right-clicking air // i see
//        handleRightClickEvent(event);
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) { // fired when right-clicking a block // oh ok
//        handleRightClickEvent(event);
//    }
//
//    private static void handleRightClickEvent(PlayerInteractEvent event) {
//        ItemStack itemStack = event.getItemStack();
//        if (RopeRestyleManager.isValidRetyping(itemStack.getItem())) {
//            if ((event.getLevel() instanceof ServerLevel level)) {
//                Player player = event.getEntity();
//                ReworkedRope rope = RopeUtils.findRope(level, player);
//                if (rope == null) return;
//
//                if (RopeRestyleManager.canRetype(rope.style, itemStack.getItem())) {
//                    RopeStyle newType = RopeRestyleManager.retype(rope.style, itemStack.getItem());
//                    if (newType == null) {
//                        return;
//                    }
//                    RopeFactory.retypeRope(level, rope.getRopeId(), newType.id());
//
//                    RopeUtils.playSound(level, rope.posData0.blockPos(), rope.style.placeSound());
//                    RopeUtils.playSound(level, rope.posData1.blockPos(), rope.style.breakSound());
//
//                    event.setCanceled(true);
//                    event.setCancellationResult(InteractionResult.SUCCESS);
//                }
//            }
//        }
//    }

    // guh
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        RopeManager.syncAllRopesToPlayer(player);
    }

}
