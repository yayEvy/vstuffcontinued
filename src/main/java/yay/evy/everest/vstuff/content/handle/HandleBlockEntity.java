package yay.evy.everest.vstuff.content.handle;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3d;
import org.joml.Vector4d;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.network.HandlePackets;
import yay.evy.everest.vstuff.network.StartHandleHoldPacket;
import yay.evy.everest.vstuff.network.StopHandleHoldPacket;

import java.util.UUID;

public class HandleBlockEntity extends KineticBlockEntity {

    private UUID holdingPlayerUuid = null;
    private static final double MAX_DISTANCE_SQ = 25.0;
    private static final double PULL_FORCE = 0.22;
    private static final Logger LOGGER = LogManager.getLogger("HandleBlockEntity");
    private static boolean lastUseKeyState = false;

    public HandleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public UUID getHoldingPlayer() {
        return holdingPlayerUuid;
    }

    public void startHolding(Player player) {
        holdingPlayerUuid = player.getUUID();
        setChanged();
        sendData();
    }

    public void stopHolding(Player player) {
        if (player.getUUID().equals(holdingPlayerUuid)) {
            holdingPlayerUuid = null;
            setChanged();
            sendData();
        }
    }

    public void forceStopHolding() {
        holdingPlayerUuid = null;
        setChanged();
        sendData();
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T be0) {
        if (!(be0 instanceof HandleBlockEntity be)) return;
        if (level.isClientSide) return;
        if (be.holdingPlayerUuid == null) return;

        ServerLevel sl = (ServerLevel) level;
        Player player = sl.getPlayerByUUID(be.holdingPlayerUuid);
        if (player == null || player.isDeadOrDying()) {
            be.forceStopHolding();
            return;
        }

        Vec3 handlePos;

        ServerShip ship = VSGameUtilsKt.getShipManagingPos(sl, pos);

        if (ship != null) {
            ShipTransform transform = ship.getShipTransform();
            Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            Vector4d world4 = new Vector4d();
            transform.getShipToWorld().transform(new Vector4d(localPos.x, localPos.y, localPos.z, 1), world4);
            handlePos = new Vec3(world4.x, world4.y, world4.z);
        } else {
            handlePos = Vec3.atCenterOf(pos);
        }

        double distSq = player.position().distanceToSqr(handlePos);
        if (distSq > MAX_DISTANCE_SQ) {
            be.forceStopHolding();
            return;
        }

        Vec3 toHandle = handlePos.subtract(player.position());
        double distance = toHandle.length();
        if (distance > 0.05) {
            double k = 0.15;

            Vec3 desiredVelocity = toHandle.scale(k);
            Vec3 currentVelocity = player.getDeltaMovement();

            double damping = 0.8;
            Vec3 newVelocity = currentVelocity.scale(damping).add(desiredVelocity.scale(1 - damping));

            player.setDeltaMovement(newVelocity);
            player.hurtMarked = true;
        }

    }

    public static <T extends BlockEntity> void clientTick(Level level, BlockPos pos, BlockState state, T be0) {
        if (!(be0 instanceof HandleBlockEntity be)) return;
        if (!level.isClientSide) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        HitResult hit = player.pick(5.0, 0, false);
        boolean lookingAtBlock = hit instanceof BlockHitResult bhr && bhr.getBlockPos().equals(pos);
        boolean useKeyPressed = mc.options.keyUse.isDown();

        if (lookingAtBlock && useKeyPressed && !lastUseKeyState) {
            if (player.getUUID().equals(be.getHoldingPlayer())) {
                HandlePackets.sendToServer(new StopHandleHoldPacket(pos));
                be.forceStopHolding();
                HandleClientHandler.setHolding(player, true, Vec3.atCenterOf(pos));
            } else {
                HandlePackets.sendToServer(new StartHandleHoldPacket(pos));
                HandleClientHandler.setHolding(player, false, null);
            }
        }

        lastUseKeyState = useKeyPressed;

        if (player.getUUID().equals(be.getHoldingPlayer())) {
            HandleClientHandler.setHolding(player, true, Vec3.atCenterOf(pos));
        } else {
            HandleClientHandler.setHolding(player, false, null);
        }
    }

    private static final String NBT_UUID = "HoldingPlayerUUID";

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        if (holdingPlayerUuid != null)
            tag.putUUID(NBT_UUID, holdingPlayerUuid);
        else
            tag.remove(NBT_UUID);
        super.write(tag, clientPacket);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        holdingPlayerUuid = tag.contains(NBT_UUID) ? tag.getUUID(NBT_UUID) : null;
        super.read(tag, clientPacket);
    }
}
