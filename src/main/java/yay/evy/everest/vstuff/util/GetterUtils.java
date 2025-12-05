package yay.evy.everest.vstuff.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;

import javax.annotation.Nullable;

public class GetterUtils {

    public static Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
    }

    public static GameToPhysicsAdapter getGTPA(ServerLevel level) {
        return ValkyrienSkiesMod.getOrCreateGTPA(ValkyrienSkies.getDimensionId(level));
    }

    public static ServerShip getServerShipFromId(ServerLevel level, Long shipId) {
        return VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
    }

    public static LoadedShip getLoadedShipFromId(ServerLevel level, Long shipId) {
        return VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
    }

    public static @Nullable LoadedShip getLoadedShipAtPos(ServerLevel level, BlockPos pos) {
        return VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
    }


}
