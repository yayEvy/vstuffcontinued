package dev.flarelog.vstuff.content.ropes.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import javax.annotation.Nullable;

public record LocalPosAndBodyId(@NotNull Vector3d pos, @Nullable Long id) {

    public LocalPosAndBodyId(@NotNull Vector3d pos, ServerLevel level) {
        this(pos, getId(pos, level));
    }

    public static Pair<LocalPosAndBodyId, LocalPosAndBodyId> create(ServerLevel level, BlockPos pos0, BlockPos pos1) {
        LocalPosAndBodyId first = new LocalPosAndBodyId(VectorConversionsMCKt.toJOMLD(pos0), level);
        LocalPosAndBodyId second = new LocalPosAndBodyId(VectorConversionsMCKt.toJOMLD(pos1), level);

        return new Pair<>(first, second);
    }

    private static Long getId(Vector3d pos, ServerLevel level) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
        return ship == null ? null : ship.getBodyId();
    }

    public boolean isWorld() {
        return this.id() == null;
    }

    public Vector3d getWorldPos(ServerLevel level) {
        if (this.id() == null) return pos;
        ServerVsBody body = ValkyrienSkies.api().getServerShipWorld(level.getServer()).getAllBodies().getById(this.id());
        return body.getKinematics().getTransform().getToWorld().transformPosition(pos);
    }

}
