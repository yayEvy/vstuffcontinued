package yay.evy.everest.vstuff.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.infrastructure.data.RopeRestyleReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.RopeStyleCategoryReloadListener;
import yay.evy.everest.vstuff.infrastructure.data.RopeStyleReloadListener;
import yay.evy.everest.vstuff.internal.RopeRestyleManager;
import yay.evy.everest.vstuff.internal.RopeStyleManager;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RopeStyleReloadListener());
        event.addListener(new RopeStyleCategoryReloadListener());
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

    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        handleRightClickEvent(event);
    }

//    @SubscribeEvent
//    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
//        handleRightClickEvent(event);
//    }

    private static void handleRightClickEvent(PlayerInteractEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();

        ReworkedRope rope = RopeUtils.findTargetedLead(level, player);
        if (rope == null) return;

        RopeStyleManager.RopeStyle newStyle = RopeRestyleManager.restyle(rope.style, itemStack.getItem());

        RopeFactory.restyleRope(level, rope.getRopeId(), newStyle);
    }
}
