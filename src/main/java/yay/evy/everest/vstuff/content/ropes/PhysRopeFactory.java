package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.packet.AddRopePacket;
import yay.evy.everest.vstuff.content.ropes.packet.PhysRopeSegmentsPacket;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.utility.RopePosData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysRopeFactory {


    @Nullable
    public static PhysRopeConstraint createPhysRope(ServerLevel level, BlockPos pos0, BlockPos pos1,
                                                    Long ship0, Long ship1, Player player) {
        try {
            kotlin.Pair<RopePosData, RopePosData> posDataPair = RopePosData.create(level, ship0, ship1, pos0, pos1);
            RopePosData posData0 = posDataPair.component1();
            RopePosData posData1 = posDataPair.component2();

            double actualLength = posData0.getWorldPos(level).distance(posData1.getWorldPos(level));

            int id = PhysRopeManager.get(level).allocateId();
            PhysRopeConstraint constraint = new PhysRopeConstraint(posData0, posData1);
            constraint.create(level, id);

            PhysRopeManager.get(level).addRope(constraint);

            posData0.attach(level, id);
            posData1.attach(level, id);

            VStuffPackets.channel().send(PacketDistributor.ALL.noArg(),
                    new AddRopePacket(id, posData0.shipId(), posData1.shipId(),
                            posData0.localPos(), posData1.localPos(),
                            actualLength, new ResourceLocation("vstuff", "normal")));

            VStuff.LOGGER.info("Phys rope {} created between {} and {}", id, pos0, pos1);
            return constraint;
        } catch (Exception e) {
            VStuff.LOGGER.error("Failed to create phys rope: {}", e.getMessage());
            return null;
        }
    }

}