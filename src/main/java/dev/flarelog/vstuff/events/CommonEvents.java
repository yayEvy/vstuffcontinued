package dev.flarelog.vstuff.events;

import dev.flarelog.vstuff.content.physics.ships.nail.NailItem;
import dev.flarelog.vstuff.internal.utility.FixedConstraintUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.commands.VStuffCommands;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.util.RopeUtil;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        Vector3d worldBreakPos = RopeUtil.getWorldPos(level, brokenPos);
        Map<Integer, ResourceKey<RopeStyle>> idsToRemove = new HashMap<>();

        // todo reimplement
    }
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

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
    }

    @SubscribeEvent
    public static void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) throws InterruptedException {

        Level level = event.getEntity().level();

        BlockPos blockToBePhysd = event.getPos();                          //the yapper that becomes a ship / gets constrained to other block
        BlockPos blockToBeNailedTo = NailItem.posMap.get(blockToBePhysd);  // the yapper that is/isnt a ship and gets the other block nailed to it

        NailItem.posMap.remove(blockToBePhysd);

        if (blockToBeNailedTo != null) {
            if (level instanceof ServerLevel serverLevel) {
                System.out.println("ASSEMBLING " + blockToBePhysd);

                ServerShip ship = ShipAssembler.assembleToShip(serverLevel, Set.of(blockToBePhysd), 1);
                //Thread.sleep(11);
                FixedConstraintUtils.createFixedConstraint(serverLevel, blockToBeNailedTo, ship, NailItem.coolDirection);


            } else System.out.println("Level: " + level + " isnt serverlevel");

        }
    }
}
