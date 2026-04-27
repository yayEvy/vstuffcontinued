package yay.evy.everest.vstuff.content.physics.levituff;

import net.minecraft.util.Mth;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.core.impl.shadow.FU;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;

import java.util.Set;

import static org.joml.Math.lerp;

public enum LevituffBehavior {
    UP_TO_Y_LEVEL((level, ship, levituffBlocks) -> {
        final double gravity = -level.getGravity().y();

        final double strengthMult = VStuffConfigs.server().levituffStrengthMultiplier.get();

        levituffBlocks.forEach(pos -> {
            final double worldY = ship.getTransform().getShipToWorld().transformPosition(new Vector3d(pos)).y();
            final double error = 256.0 - worldY;

            final double spring = error * strengthMult * ship.getMass() * gravity;
            final double damping = -ship.getVelocity().y() * ship.getMass() * VStuffConfigs.server().levituffForceDamping.get() / levituffBlocks.size();

            final double forceY = spring + damping;
            ship.applyModelForce(new Vector3d(0, forceY, 0), pos);
        });
    }),
    UNSTOPPABLE_WHIMSY((level, ship, levituffBlocks) -> {
        final double shipY = ship.getTransform().getPositionInWorld().y();
        final double mass = ship.getMass();

        final double liftMultiplier = getLiftMultiplier(shipY) * levituffBlocks.size();

        final double velY = ship.getVelocity().y();
        final double forceDamping = VStuffConfigs.server().levituffForceDamping.get();
        final double damping = -velY * mass * forceDamping;

        ship.applyInvariantForce(new Vector3d(0, (mass * liftMultiplier) + damping, 0));
    })
    ;

    public static double getLiftMultiplier(double y) {
        if (y < 0) return lerp(1.6, 1.3, (y + 64) / 64.0);
        if (y < 100) return lerp(1.3, 1.1, y / 100.0);
        if (y < 200) return lerp(1.1, 0.9, (y - 100) / 100.0);
        if (y < 300) return lerp(0.9, 0.5, (y - 200) / 100.0);
        return 0.3;
    }


    private final TriConsumer<FU, PhysShipImpl, Set<Vector3d>> onPhysTick;

    LevituffBehavior(TriConsumer<FU, PhysShipImpl, Set<Vector3d>> onPhysTick) {
        this.onPhysTick = onPhysTick;
    }

    public void physTick(PhysLevel level, PhysShip ship, Set<Vector3d> levituffBlocks) {
        this.onPhysTick.accept((FU) level, (PhysShipImpl) ship, levituffBlocks);
    }
}
