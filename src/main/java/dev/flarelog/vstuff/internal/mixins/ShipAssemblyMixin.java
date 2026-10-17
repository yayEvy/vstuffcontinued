
package dev.flarelog.vstuff.internal.mixins;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.Rope;
import dev.flarelog.vstuff.content.ropes.RopeFactory;
import dev.flarelog.vstuff.content.ropes.RopeManager;
import dev.flarelog.vstuff.content.ropes.data.LocalPosAndBodyId;
import dev.flarelog.vstuff.internal.utility.VSUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(ShipAssembler.class)
public class ShipAssemblyMixin {

    @Inject(method = "assembleToShipFull", at = @At("RETURN"), remap = false)
    private static void onAssembleToShipFull(
            ServerLevel level,
            Set<BlockPos> blocks,
            double scale,
            CallbackInfoReturnable<ShipAssembler.AssembleContext> cir) {

        ShipAssembler.AssembleContext ctx = cir.getReturnValue();
        if (ctx == null || ctx.getShip() == null)
            return;

        ServerShip newShip = ctx.getShip();
        Long newShipId = newShip.getId();
        RopeManager manager = RopeManager.get(level);

        List<RopeReattachment> affectedRopes = new ArrayList<>();

        for (Rope rope : manager.getRopeList()) {
            BlockPos pos0 = rope.getPosData0().blockPos(level);
            BlockPos pos1 = rope.getPosData1().blockPos(level);

            boolean end0 = blocks.contains(pos0);
            boolean end1 = blocks.contains(pos1);

            if (!end0 && !end1)
                continue;

            Vector3d worldPos0 = rope.getPosData0().getWorldPos(level);
            Vector3d worldPos1 = rope.getPosData1().getWorldPos(level);

            affectedRopes.add(new RopeReattachment(
                    rope,
                    end0,
                    end1,
                    worldPos0,
                    worldPos1
            ));
        }

        if (affectedRopes.isEmpty())
            return;

        level.getServer().execute(() -> {
            for (RopeReattachment attachment : affectedRopes) {
                Rope rope = attachment.rope();

                try {
                    LocalPosAndBodyId data0 = attachment.end0()
                            ? new LocalPosAndBodyId(
                            VSUtils.worldToShipLocal(
                                    level,
                                    attachment.worldPos0(),
                                    newShipId
                            ),
                            newShipId
                    )
                            : rope.getPosData0();

                    LocalPosAndBodyId data1 = attachment.end1()
                            ? new LocalPosAndBodyId(
                            VSUtils.worldToShipLocal(
                                    level,
                                    attachment.worldPos1(),
                                    newShipId
                            ),
                            newShipId
                    )
                            : rope.getPosData1();

                    var styleKey = rope.getStyleKey();
                    var type = rope.getType();

                    RopeFactory.removeAndCleanupRope(rope, level);

                    RopeFactory.createRope(
                            new RopeFactory.RopeContext(level, data0, data1),
                            styleKey,
                            type
                    );

                } catch (Exception e) {
                    VStuff.LOGGER.error(
                            "[VStuff] Failed to reattach rope {} after ship assembly",
                            rope.getRopeId(),
                            e
                    );
                }
            }
        });
    }

    private record RopeReattachment(
            Rope rope,
            boolean end0,
            boolean end1,
            Vector3d worldPos0,
            Vector3d worldPos1
    ) {}
}
