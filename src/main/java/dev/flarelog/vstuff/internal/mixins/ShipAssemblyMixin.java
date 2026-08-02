package dev.flarelog.vstuff.internal.mixins;

import dev.flarelog.vstuff.content.ropes.Rope;
import dev.flarelog.vstuff.content.ropes.RopeManager;
import dev.flarelog.vstuff.content.ropes.util.LocalPosAndBodyId;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.util.RopeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ShipAssembler.class)
public class ShipAssemblyMixin {

    @Inject(method = "assembleToShipFull", at = @At("RETURN"), remap = false)
    private static void onAssembleToShipFull(
            ServerLevel level, Set<BlockPos> blocks, double scale,
            CallbackInfoReturnable<ShipAssembler.AssembleContext> cir) {

        ShipAssembler.AssembleContext ctx = cir.getReturnValue();
        if (ctx == null) return;

        ServerShip newShip = ctx.getShip();

        Long newShipId = newShip.getId();
        RopeManager manager = RopeManager.get(level);

        QueryableShipData<Ship> allShips = ValkyrienSkies.api().getShipWorld(level).getAllShips();

        Set<Rope> affectedRopes = new HashSet<>();
        Set<Rope> pos0Affected = new HashSet<>();
        Set<Rope> pos1Affected = new HashSet<>();
        Set<Rope> bothAffected = new HashSet<>();

        for (Rope rope : manager.getRopeList()) {
            boolean pos0Hit = allShips.contains(rope.posData0.id()) && blocks.contains(rope.posData0.blockPos());
            boolean pos1Hit = allShips.contains(rope.posData1.id()) && blocks.contains(rope.posData1.blockPos());

            if (pos0Hit && pos1Hit) {
                bothAffected.add(rope);
            } else if (pos0Hit) {
                pos0Affected.add(rope);
            } else if (pos1Hit) {
                pos1Affected.add(rope);
            }
        }

        if (pos0Affected.isEmpty() && pos1Affected.isEmpty() && bothAffected.isEmpty()) return;

        level.getServer().execute(() -> {
            for (Rope rope : affectedRopes) {
                try {
                    boolean end0 = blocks.contains(rope.posData0.blockPos());
                    boolean end1 = blocks.contains(rope.posData1.blockPos());

                    Vector3d worldPos0 = rope.posData0.getWorldPos(level);
                    Vector3d worldPos1 = rope.posData1.getWorldPos(level);

                    Vector3d newLocal0 = end0 ? RopeUtil.worldToShipLocal(level, worldPos0, newShipId) : rope.posData0.pos();
                    Vector3d newLocal1 = end1 ? RopeUtil.worldToShipLocal(level, worldPos1, newShipId) : rope.posData1.pos();



                    // todo reimplement

                } catch (Exception e) {
                    VStuff.LOGGER.error("[VStuff] Failed to reattach rope {} after ship assembly: {}",
                            rope.getRopeId(), e.getMessage());
                }
            }
        });
    }

//    private void reattachEnd0(Level level, Rope rope, long newShipId) {
//        Vector3d worldPos0 = rope.posData0.getWorldPos(level);
//        Vector3d newLocal0 = RopeUtil.worldToShipLocal(level, worldPos0, newShipId);
//        rope.posData0 = new LocalPosAndBodyId(newLocal0, newShipId);
//    }
//
//    private void reattachEnd1(Level level, Rope rope, long newShipId) {
//        Vector3d worldPos1 = rope.posData1.getWorldPos(level);
//        Vector3d newLocal1 = RopeUtil.worldToShipLocal(level, worldPos1, newShipId);
//        rope.setPosData(rope.posData0, new LocalPosAndBodyId(newLocal1, newShipId));
//    }

}