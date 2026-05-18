package yay.evy.everest.vstuff.content.physics.physgrabber;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import yay.evy.everest.vstuff.content.physics.physgrabber.packet.GrabPacket;
import yay.evy.everest.vstuff.content.physics.physgrabber.packet.ReleasePacket;
import yay.evy.everest.vstuff.content.physics.physgrabber.packet.UpdatePacket;
import yay.evy.everest.vstuff.index.VStuffSounds;
import yay.evy.everest.vstuff.index.VStuffPackets;

public class PhysGrabberClientHandler{

    private static LoadedShip grabbedShip = null;
    private static SoundInstance humSound = null;
    private static float grabDistance = 5.0f;


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
                Vec3 hitLocation = blockHit.getLocation();
                grabDistance = (float) player.getEyePosition(1.0F).distanceTo(hitLocation);

                org.joml.Vector3d jomlHit = new org.joml.Vector3d(hitLocation.x, hitLocation.y, hitLocation.z);
                var localHit = ship.getWorldToShip().transformPosition(jomlHit);

                Vec3 target = player.getEyePosition(1.0F).add(player.getLookAngle().scale(grabDistance));

                VStuffPackets.channel().sendToServer(new GrabPacket(ship.getId(), target, localHit, player.isCreative()));

                grabbedShip = ship;

                if (mc.level != null) {
                    humSound = new GrabberHum(VStuffSounds.GRABBER_HUM.get(), ship, player);
                    mc.getSoundManager().play(humSound);
                }
                return true;
            }
            return false;
        } else {
            VStuffPackets.channel().sendToServer(new ReleasePacket(grabbedShip.getId()));
            grabbedShip = null;

            if (humSound instanceof GrabberHum hum) {
                hum.startStopping();
                humSound = null;
            }
            return true;
        }
    }

    public static void forceRelease(Minecraft mc, Player player) {
        if (grabbedShip != null) {
            VStuffPackets.channel().sendToServer(new ReleasePacket(grabbedShip.getId()));
            grabbedShip = null;

            if (humSound instanceof GrabberHum hum) {
                hum.startStopping();
                humSound = null;
            }
        }
    }

    public static void tickClient(Minecraft mc, Player player) {
        if (grabbedShip == null) return;

        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 target = eyePos.add(player.getLookAngle().scale(grabDistance));
        VStuffPackets.channel().sendToServer(new UpdatePacket(grabbedShip.getId(), target, player.isCreative()));
    }

    public static void changeDistance(double y){
        if(grabbedShip != null) {
            float y2 = (float)(grabDistance + y);

            if (y2 <= 2.0f) y2 = 2.0f; ///todo: make these configable
            if (y2 >= 25.0f) y2 = 25.0f;

            grabDistance = y2;
        }
    }

    public static Vec3 getGrabbedShipPos(float partialTicks) {
        if (grabbedShip == null) return null;

        var transform = grabbedShip.getShipTransform();
        if (transform == null) return null;

        var shipWorldPos = transform.getPositionInWorld();
        return new Vec3(shipWorldPos.x(), shipWorldPos.y(), shipWorldPos.z());
    }

    public static boolean isHoldingGrabber(Player player){
        if (player.getMainHandItem().getItem() instanceof PhysGrabberItem) return true;

        else return false;
    }

    public static LoadedShip getGrabbedShip() {
        return grabbedShip;
    }

    public static boolean isGrabbing(Player player) {
        return grabbedShip != null;
    }

    public static boolean isGrabbing() {
        return grabbedShip != null;
    }

}
