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

    public Set<Vector3d> levituffBlocks = new HashSet<>();

    LevituffAttachment() {}


    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        if (levituffBlocks.isEmpty()) return;

        PhysShipImpl ship = (PhysShipImpl) physShip;
        FU level = (FU) physLevel; // fuck you or smthn idk

        double gravity = -level.getGravity().y();

        double damping = -ship.getVelocity().y() * ship.getMass() * VStuffConfigs.server().levituffForceDamping.get();
        double strengthMult = VStuffConfigs.server().levituffStrengthMultiplier.get() * 1024;

        Vector3d dampingForce = new Vector3d(0, damping, 0);

        levituffBlocks.forEach(pos -> {
            double t = Math.max(0.0, Math.max(1.0, ship.getTransform().getShipToWorld().transformPosition(pos).y() / 256));

            ship.applyWorldForceToModelPos(new Vector3d(0, strengthMult * (1.0 - (t*t)) * gravity, 0), pos);
            ship.applyWorldForceToModelPos(dampingForce, pos);
        });
    }

    private static Vector3d toV3D(BlockPos pos) {
        return new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public void addBlock(BlockPos pos) {
        levituffBlocks.add(toV3D(pos));
    }

    public void removeBlock(BlockPos pos) {
        levituffBlocks.remove(toV3D(pos));
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