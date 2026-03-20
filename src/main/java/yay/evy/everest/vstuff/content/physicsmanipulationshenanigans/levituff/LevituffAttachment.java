package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import yay.evy.everest.vstuff.content.ships.thrust.AttachmentUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LevituffAttachment implements ShipPhysicsListener {

    public Map<Long, LevituffForceApplier> appliersMapping = new ConcurrentHashMap<>();

    LevituffAttachment() {}

    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {

        if (appliersMapping.isEmpty())
            return;

        PhysShipImpl ship = (PhysShipImpl) physShip;

        double shipY = ship.getTransform().getPositionInWorld().y();
        double mass = ship.getMass();

        double gravityForce = mass * 9.81;

        double liftFactor = 0.0;

        for (LevituffForceApplier applier : appliersMapping.values()) {
            liftFactor += applier.baseStrength * applier.getLiftMultiplier(shipY);
        }

        liftFactor = liftFactor / (1.0 + liftFactor);

        liftFactor *= 1.5;

        double liftForce = gravityForce * liftFactor;

        double verticalVelocity = ship.getVelocity().y();

        double damping = -verticalVelocity * mass * 1.2;
        double buoyancy = liftForce - gravityForce;
        double totalForce = buoyancy + damping;

        ship.applyInvariantForce(new Vector3d(0, totalForce, 0));
    }

    public void addApplier(BlockPos pos, LevituffForceApplier applier) {
        appliersMapping.put(pos.asLong(), applier);
    }

    public void removeApplier(ServerLevel level, BlockPos pos) {
        appliersMapping.remove(pos.asLong());
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