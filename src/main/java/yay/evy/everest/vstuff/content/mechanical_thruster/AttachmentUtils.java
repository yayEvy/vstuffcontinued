package yay.evy.everest.vstuff.content.mechanical_thruster;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class AttachmentUtils {
    private AttachmentUtils() {}


    @Nullable
    public static LoadedServerShip getShipAt(ServerLevel level, BlockPos pos) {
        Ship s = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        if (s instanceof LoadedServerShip loadedShip) {
            return loadedShip;
        }
        return null;
    }


    public static <T> T getOrCreate(LoadedServerShip ship, Class<T> attachmentClass, Supplier<T> factory) {
        T attachment = ship.getAttachment(attachmentClass);
        if (attachment == null) {
            attachment = factory.get();
            ship.setAttachment(attachmentClass, attachment);
        }
        return attachment;
    }

    @Nullable
    public static <T> T get(Level level, BlockPos pos, Class<T> attachmentClass, Supplier<T> factory) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        LoadedServerShip ship = getShipAt(serverLevel, pos);


        return ship != null ? getOrCreate(ship, attachmentClass, factory) : null;
    }
}