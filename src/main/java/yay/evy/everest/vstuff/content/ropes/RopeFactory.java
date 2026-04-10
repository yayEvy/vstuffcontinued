package yay.evy.everest.vstuff.content.ropes;

import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import yay.evy.everest.vstuff.content.ropes.packet.UpdateRopeStylePacket;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.*;
import yay.evy.everest.vstuff.internal.utility.records.JointValues;
import yay.evy.everest.vstuff.internal.utility.records.RopePosData;

public class  RopeFactory {

    public record RopeResult(ReworkedRope rope, boolean valid, String message) {
        public static RopeResult withMessage(String message) {
            return new RopeResult(null, false, message);
        }

        public static RopeResult validResult(ReworkedRope rope) {
            return new RopeResult(rope, true, null);
        }
    }

    public static RopeResult tryCreateNewRope(ServerLevel level, ItemStack ropeItem, BlockPos blockPos0, BlockPos blockPos1, Player player) {
        CompoundTag tag = ropeItem.getOrCreateTagElement("data");
        Long ship0 = ShipUtils.getLoadedShipIdAtPos(level, blockPos0);
        Long ship1 = ShipUtils.getLoadedShipIdAtPos(level, blockPos1);
        String originDimension = tag.getString("dim");

        float length = (float) RopeUtils.getWorldPos(level, blockPos0, ship0).distance(RopeUtils.getWorldPos(level, blockPos1, ship1)) + 0.5f;

        if (!originDimension.equals(level.dimension().location().toString()))
            return RopeResult.withMessage("message.interdimensional_fail");
        if (length > VStuffConfigs.server().ropeMaxLength.get())
            return RopeResult.withMessage("message.too_long");
        if (!IRopeActor.canAttach(level.getBlockState(blockPos1)))
            return RopeResult.withMessage("message.actor_connected");

        return RopeResult.validResult(createNewRope(
                level,
                ship0,
                ship1,
                blockPos0,
                blockPos1,
                RopeStyle.getOrDefaultStyleId(ropeItem.getOrCreateTag()),
                player
        ));
    }

    public static ReworkedRope createNewRope(ServerLevel level, Long ship0, Long ship1, BlockPos blockPos0, BlockPos blockPos1, ResourceLocation style, Entity player) {
        Pair<RopePosData, RopePosData> posDataPair = RopePosData.create(level, ship0, ship1, blockPos0, blockPos1);
        RopePosData posData0 = posDataPair.component1();
        RopePosData posData1 = posDataPair.component2();

        float length = (float) posData0.getWorldPos(level).distance(posData1.getWorldPos(level)) + 0.5f;

        float mass0 = ShipUtils.getMassForShip(level, ship0);
        float mass1 = ShipUtils.getMassForShip(level, ship1);
        // double compliance = 1e-12 / Math.max(Math.min(mass0, mass1), 100.0) * (posData0.isWorld() || posData1.isWorld() ? 0.05 : 1); maybe not needed idk
        float maxForce = 5e13f * Math.min(Math.max(mass0, mass1) / Math.min(mass0, mass1), 20.0f) * (posData0.isWorld() || posData1.isWorld() ? 10f : 1f);

        ReworkedRope rope = new ReworkedRope(posData0, posData1, JointValues.withDefault(maxForce, maxForce, length), style);

        if (!rope.hasJoint) {
            RopeManager.get(level).addRope(rope);
            //System.out.println("attach actors");
            rope.attachActors(level);

            if (player instanceof ServerPlayer serverPlayer) {
                RopeManager.syncAllRopesToPlayer(serverPlayer);
            }
        }
        else
            GTPAUtils.addRopeJoint(level, rope, player);

        return rope;
    }

    public static void removeRope(ServerLevel serverLevel, Integer ropeId) {
        RopeManager manager = RopeManager.get(serverLevel);
        ReworkedRope removed = manager.getRope(ropeId);

        if (removed.hasJoint) {
            GTPAUtils.removeJoint(serverLevel, removed);
        } else {
            manager.removeRope(ropeId);
            removed.detachActors(serverLevel);
        }
    }

    public static void retypeRope(ServerLevel serverLevel, Integer ropeId, ResourceLocation newTypeId) {
        ReworkedRope rope = RopeManager.get(serverLevel).getRope(ropeId);
        if (rope == null) return;

        rope.style = RopeStyleManager.get(newTypeId);

        //NetworkHandler.sendRopeUpdate(ropeId, rope.posData0.shipId(), rope.posData1.shipId(), rope.posData0.localPos(), rope.posData1.localPos(), rope.jointValues.maxLength(), rope.style.id());

        VStuffPackets.channel().send(PacketDistributor.ALL.noArg(), new UpdateRopeStylePacket(ropeId, rope.style.id()));
    }

    public static CompoundTag ropeToTag(ReworkedRope rope) {
        CompoundTag ropeTag = new CompoundTag();

        ropeTag.putInt("ropeId", rope.ropeId);
        ropeTag.putInt("jointId", rope.getJointId());
        ropeTag.put("posData0", TagUtils.writePosData(rope.posData0));
        ropeTag.put("posData1", TagUtils.writePosData(rope.posData1));
        ropeTag.put("jointValues", TagUtils.writeJointValues(rope.jointValues));
        ropeTag.put("style", TagUtils.writeResourceLocation(rope.style.id()));

        return ropeTag;
    }

    public static ReworkedRope ropeFromTag(CompoundTag ropeTag) {
        ReworkedRope rope = new ReworkedRope(
                TagUtils.readPosData(ropeTag.getCompound("posData0")),
                TagUtils.readPosData(ropeTag.getCompound("posData1")),
                TagUtils.readJointValues(ropeTag.getCompound("jointValues")),
                TagUtils.readResourceLocation(ropeTag.getCompound("style"))
        ).setRopeId(ropeTag.getInt("ropeId"));

        if (ropeTag.getInt("jointId") != -1) {
            rope.setJointId(ropeTag.getInt("jointId"));
        }

        return rope;
    }


}
