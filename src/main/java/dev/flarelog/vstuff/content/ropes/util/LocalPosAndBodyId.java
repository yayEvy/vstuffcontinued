package dev.flarelog.vstuff.content.ropes.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
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
import java.util.Optional;

public record LocalPosAndBodyId(@NotNull Vector3d pos, @Nullable Long id) {

    public static final Codec<LocalPosAndBodyId> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.VECTOR3D.fieldOf("pos").forGetter(LocalPosAndBodyId::pos),
                    Codec.LONG.optionalFieldOf("id")
                            .xmap(opt -> opt.orElse(null), Optional::ofNullable)
                            .forGetter(LocalPosAndBodyId::id)
            ).apply(instance, LocalPosAndBodyId::new)
    );

    public LocalPosAndBodyId(@NotNull Vector3d worldPos, ServerLevel level) {
        this(resolve(worldPos, level));
    }

    private LocalPosAndBodyId(Resolved resolved) {
        this(resolved.localPos, resolved.shipId);
    }

    private static Resolved resolve(Vector3d worldPos, ServerLevel level) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, worldPos);
        if (ship == null) return new Resolved(worldPos, null);

        Vector3d localPos = new Vector3d();
        ship.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
        return new Resolved(localPos, ship.getBodyId());
    }

    private record Resolved(Vector3d localPos, Long shipId) {}

    public static LocalPosAndBodyId from(BlockPos pos, ServerLevel level) {
        return new LocalPosAndBodyId(VectorConversionsMCKt.toJOMLD(pos), level);
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

    public BlockPos blockPos() {
        return BlockPos.containing(this.pos().x(), this.pos().y(), this.pos().z());
    }

}
