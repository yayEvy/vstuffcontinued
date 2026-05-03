package yay.evy.everest.vstuff.content.ships.thrust;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import yay.evy.everest.vstuff.internal.utility.AttachmentUtils;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

public final class ThrusterForceAttachment implements ShipPhysicsListener {

    public Map<Long, ThrusterForceApplier> appliersMapping = new ConcurrentHashMap<>();

    public ThrusterForceAttachment() {}


    public void addApplier(BlockPos pos, ThrusterForceApplier applier) {
        appliersMapping.put(pos.asLong(), applier);
    }

    public void removeApplier(BlockPos pos) {
        appliersMapping.remove(pos.asLong());
    }

    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        PhysShipImpl ship = (PhysShipImpl) physShip;

        appliersMapping.forEach((pos, applier) -> {
            applier.applyForces(BlockPos.of(pos), ship);
        });
    }


}