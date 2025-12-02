package yay.evy.everest.vstuff.content.thrust;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;


@SuppressWarnings("deprecation")
public final class ThrusterForceAttachment implements ShipPhysicsListener {

    public Map<BlockPos, ThrusterForceApplier> appliersMapping = new ConcurrentHashMap<>();

    public ThrusterForceAttachment() {}


    public void addApplier(BlockPos pos, ThrusterForceApplier applier) {
        appliersMapping.put(pos, applier);
    }

    public void removeApplier(ServerLevel level, BlockPos pos) {
        appliersMapping.remove(pos);

        if (appliersMapping.isEmpty()) {
            LoadedServerShip ship = AttachmentUtils.getShipAt(level, pos);
            if (ship != null) {
                ship.setAttachment(ThrusterForceAttachment.class, null);
            }
        }
    }

    public static ThrusterForceAttachment getOrCreateAsAttachment(LoadedServerShip ship) {
        return AttachmentUtils.getOrCreate(ship, ThrusterForceAttachment.class, ThrusterForceAttachment::new);
    }

    public static ThrusterForceAttachment get(Level level, BlockPos pos) {
        return AttachmentUtils.get(level, pos, ThrusterForceAttachment.class, ThrusterForceAttachment::new);
    }

    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        PhysShipImpl ship = (PhysShipImpl) physShip;

        appliersMapping.forEach((pos, applier) -> {
            applier.applyForces(pos, ship);
        });
    }


}