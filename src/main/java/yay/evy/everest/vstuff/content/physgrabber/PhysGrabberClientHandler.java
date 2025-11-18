package yay.evy.everest.vstuff.content.physgrabber;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.core.api.ships.Ship;
import yay.evy.everest.vstuff.network.PhysGrabberNetwork;

public class PhysGrabberClientHandler {

    private static LoadedShip grabbedShip = null;

    public static boolean tryGrabOrRelease(Minecraft mc, Player player) {
        if (grabbedShip == null) {
            HitResult hit = mc.hitResult;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;

            ClientLevel level = (ClientLevel) player.level();
            BlockHitResult blockHit = (BlockHitResult) hit;

            LoadedShip ship = VSGameUtilsKt.getShipObjectManagingPos(
                    level,
                    VectorConversionsMCKt.toJOMLD(blockHit.getBlockPos())
            );

            if (ship != null) {
                grabbedShip = ship;
               // System.out.println("[PhysGrabberClient] Grabbing ship " + ship.getId());

                Vec3 target = player.getEyePosition(1.0F).add(player.getLookAngle().scale(5.0));

                boolean creative = player.isCreative(); // determine gamemode
                PhysGrabberNetwork.sendGrab(ship.getId(), target, creative);
                PhysGrabberNetwork.sendUpdate(ship.getId(), target, creative);
                return true;
            }
            return false;
        } else {
            //System.out.println("[PhysGrabberClient] Releasing ship " + grabbedShip.getId());
            PhysGrabberNetwork.sendRelease(grabbedShip.getId());
            grabbedShip = null;
            return true;
        }
    }


    public static void tickClient(Minecraft mc, Player player) {
        if (grabbedShip == null) return;

        Vec3 eyePos = player.getEyePosition(1.0F);
        if (mc.options.getCameraType().isFirstPerson()) {
            eyePos = eyePos.add(player.getLookAngle().scale(0.5));
        }

        Vec3 lookDir = player.getLookAngle();
        Vec3 target = eyePos.add(lookDir.scale(5.0)).add(0, 0.5, 0);

        boolean creative = player.isCreative(); // get gamemode
        PhysGrabberNetwork.sendUpdate(grabbedShip.getId(), target, creative);
    }


    public static Vec3 getGrabbedShipPos(float partialTicks) {
        if (grabbedShip == null) return null;

        var transform = grabbedShip.getShipTransform();
        if (transform == null) return null;

        var shipWorldPos = transform.getPositionInWorld();
        return new Vec3(shipWorldPos.x(), shipWorldPos.y(), shipWorldPos.z());
    }

    public static LoadedShip getGrabbedShip() {
        return grabbedShip;
    }

    public static boolean isGrabbing(Player player) {
        return grabbedShip != null;
    }

}
