package yay.evy.everest.vstuff.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.internal.utility.GTPAUtils;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.ArrayList;
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
        if (newShip == null) return;

        Long newShipId = newShip.getId();
        RopeManager manager = RopeManager.get(level);

        List<ReworkedRope> affectedRopes = new ArrayList<>();
        for (ReworkedRope rope : manager.getRopeList()) {
            if (blocks.contains(rope.posData0.blockPos()) || blocks.contains(rope.posData1.blockPos()))
                affectedRopes.add(rope);
        }

        if (affectedRopes.isEmpty()) return;

        level.getServer().execute(() -> {
            for (ReworkedRope rope : affectedRopes) {
                try {
                    boolean end0 = blocks.contains(rope.posData0.blockPos());
                    boolean end1 = blocks.contains(rope.posData1.blockPos());

                    Vector3d worldPos0 = RopeUtils.getWorldPos(level, rope.posData0.blockPos(), rope.posData0.shipId());
                    Vector3d worldPos1 = RopeUtils.getWorldPos(level, rope.posData1.blockPos(), rope.posData1.shipId());

                    Vector3d newLocal0 = end0 ? RopeUtils.worldToShipLocal(level, worldPos0, newShipId) : rope.posData0.localPos();
                    Vector3d newLocal1 = end1 ? RopeUtils.worldToShipLocal(level, worldPos1, newShipId) : rope.posData1.localPos();

                    if (rope.hasJoint && rope.hasTrackedJoint())
                        GTPAUtils.removeJoint(level, rope);
                    else {
                        rope.detachActors(level);
                        manager.removeRope(rope.getRopeId());
                    }

                    RopeFactory.reCreateNewRope(
                            level,
                            end0 ? newShipId : rope.posData0.shipId(),
                            end1 ? newShipId : rope.posData1.shipId(),
                            RopeUtils.containingBlockPos(newLocal0),
                            RopeUtils.containingBlockPos(newLocal1),
                            rope.style.id(), null);

                } catch (Exception e) {
                    VStuff.LOGGER.error("[VStuff] Failed to reattach rope {} after ship assembly: {}",
                            rope.getRopeId(), e.getMessage());
                }
            }
        });
    }
}