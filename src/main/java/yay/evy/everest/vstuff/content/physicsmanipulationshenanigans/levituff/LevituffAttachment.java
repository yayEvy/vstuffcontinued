package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.valkyrienskies.core.impl.shadow.FU;
import yay.evy.everest.vstuff.content.ships.thrust.AttachmentUtils;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LevituffAttachment implements ShipPhysicsListener {

    public Set<BlockPos> levituffBlocks = new HashSet<>();

    LevituffAttachment() {}

    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        if (levituffBlocks.isEmpty()) return;

        PhysShipImpl ship = (PhysShipImpl) physShip;
        FU level = (FU) physLevel; // fuck you or smthn idk

        double mass = ship.getMass();

        for (BlockPos pos : levituffBlocks) {
            double shipY = ship.getTransform().getShipToWorld().transformPosition(toV3D(pos)).y();
            Vector3dc gravity = level.getGravity();

            double verticalVelocity = ship.getVelocity().y();

            double liftMultiplier = getLiftMultiplier(shipY);

            double liftForce = getStrengthMult() * 1024 * liftMultiplier;

            double damping = -verticalVelocity * getDamping() * mass;

            Vector3d force = new Vector3d(0, liftForce * -gravity.y(), 0);
            Vector3d dampingForce = new Vector3d(0, damping, 0);
            Vector3d forcePos = toV3D(pos);

            ship.applyWorldForceToModelPos(force, forcePos);
            ship.applyWorldForceToModelPos(dampingForce, forcePos);
        }
    }

    private static Vector3d toV3D(BlockPos pos) {
        return new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    private static double getStrengthMult() {
        return VStuffConfigs.server().levituffStrengthMultiplier.get();
    }

    private static double getDamping() {
        return VStuffConfigs.server().levituffForceDamping.get();
    }

    public double getLiftMultiplier(double y) {
        double t = Math.max(0.0, Math.min(1.0, y / 256));

        return 1.0 - (t * t);
    }

    public void addBlock(BlockPos pos) {
        levituffBlocks.add(pos);
    }

    public void removeBlock(BlockPos pos) {
        levituffBlocks.remove(pos);
    }

    public static LevituffAttachment getOrCreateAsAttachment(LoadedServerShip ship) {
        return AttachmentUtils.getOrCreate(
                ship,
                LevituffAttachment.class,
                LevituffAttachment::new
        );
    }

    public static LevituffAttachment get(Level level, BlockPos pos) {
        return AttachmentUtils.get(
                level,
                pos,
                LevituffAttachment.class,
                LevituffAttachment::new
        );
    }
}