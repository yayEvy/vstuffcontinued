package yay.evy.everest.vstuff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.core.api.ships.Ship;
import yay.evy.everest.vstuff.network.PhysGrabberNetwork;

public class PhysGrabberClientHandler {

    private static Ship grabbedShip = null;

    public static void tryGrabOrRelease(Minecraft mc, Player player) {
        if (grabbedShip == null) {
            HitResult hit = mc.hitResult;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

            ClientLevel level = (ClientLevel) player.level();
            BlockHitResult blockHit = (BlockHitResult) hit;

            Ship ship = VSGameUtilsKt.getShipObjectManagingPos(
                    level,
                    VectorConversionsMCKt.toJOMLD(blockHit.getBlockPos())
            );

            if (ship != null) {
                grabbedShip = ship;
                System.out.println("[PhysGrabberClient] Grabbing ship " + ship.getId());

                Vec3 target = player.getEyePosition(1.0F).add(player.getLookAngle().scale(5.0));
                PhysGrabberNetwork.sendGrab(ship.getId(), target);
            }
        } else {
            System.out.println("[PhysGrabberClient] Releasing ship " + grabbedShip.getId());
            PhysGrabberNetwork.sendRelease(grabbedShip.getId());
            grabbedShip = null;
        }
    }

    public static void tickClient(Minecraft mc, Player player) {
        if (grabbedShip == null) return;

        Vec3 eyePos = player.getEyePosition(1.0F);

        if (mc.options.getCameraType().isFirstPerson()) {
            eyePos = eyePos.add(player.getLookAngle().scale(0.5));
        }

        Vector3dc shipPosDC = grabbedShip.getShipTransform().getPositionInWorld();
        Vec3 shipPos = new Vec3(shipPosDC.x(), shipPosDC.y(), shipPosDC.z());

        Vec3 lookDir = player.getLookAngle();
        Vec3 target = eyePos.add(lookDir.scale(5.0)).add(0, 0.5, 0);
        PhysGrabberNetwork.sendUpdate(grabbedShip.getId(), target);

        ClientLevel level = mc.level;
        if (level == null) return;

        Vec3 dir = shipPos.subtract(eyePos);
        int steps = 20;
        long time = System.currentTimeMillis();
        double offset = (time % 1000) / 1000.0;

        double maxDistance = dir.length();

        for (int i = 0; i <= steps; i++) {
            double t = (i / (double) steps + offset) % 1.0;

            double arcHeight = Math.sin(t * Math.PI) * 0.15;

            double wobbleX = Math.sin(time / 200.0 + t * Math.PI * 6) * 0.03;
            double wobbleZ = Math.cos(time / 250.0 + t * Math.PI * 5) * 0.03;
            float brightness = 0.3f + (float)(Math.sin(time / 150.0 + t * Math.PI * 4) * 0.2f);

            Vec3 pos = eyePos.add(dir.scale(t)).add(wobbleX, arcHeight, wobbleZ);

            double distanceToPlayer = pos.distanceTo(eyePos);
            float alpha = brightness * (float)(1.0 - distanceToPlayer / maxDistance);
            if (alpha <= 0.05f) continue;

            level.addParticle(
                    new DustParticleOptions(new Vector3f(1.0f, 0.5f, 1.0f), alpha),
                    pos.x, pos.y, pos.z,
                    0, 0.01, 0
            );
        }
    }


}
