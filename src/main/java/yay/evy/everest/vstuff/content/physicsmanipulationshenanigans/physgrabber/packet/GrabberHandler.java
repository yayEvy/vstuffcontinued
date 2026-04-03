package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.physgrabber.packet;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.physgrabber.PhysGrabberServerAttachment;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;

public class GrabberHandler {

    public static void handleGrab(ServerPlayer sender, long shipId, Vector3d initialTarget, boolean creative) {
        if (sender == null) return;
        ServerLevel level = sender.serverLevel();
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) return;

        PhysGrabberServerAttachment.getOrCreate(ship).target(initialTarget).creative(creative);
    }

    public static void handleUpdate(ServerPlayer sender, long shipId, Vector3d newTarget, boolean creative) {
        if (sender == null) return;
        ServerLevel level = sender.serverLevel();
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) return;

        PhysGrabberServerAttachment.getOrCreate(ship).target(newTarget);

        double mass = ship.getInertiaData().getMass();
        double maxMass = VStuffConfigs.server().physGrabberMaxMass.get();

        if (mass > maxMass && !creative) {
            sender.displayClientMessage(
                    Component.translatable("vstuff.message.grabber_limit").append(" (" + mass + " / " + maxMass + ")").withStyle(ChatFormatting.RED),
                    true
            );
            PhysGrabberServerAttachment.getOrCreate(ship).release();
            return;
        }

        PhysGrabberServerAttachment.getOrCreate(ship).target(newTarget).creative(creative);
    }


    public static void handleRelease(ServerPlayer sender, long shipId) {
        if (sender == null) return;
        ServerLevel level = sender.serverLevel();
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) return;

        PhysGrabberServerAttachment.getOrCreate(ship).release();
    }
}
