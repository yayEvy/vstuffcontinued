package yay.evy.everest.vstuff.internal.utility;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.valkyrienskies.core.api.ships.LoadedServerShip;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;

public class AttachmentUtils {

    @Nullable
    public static <A extends ShipPhysicsListener> A getOrCreateAttachment(Level level, BlockPos pos, Class<A> attachmentClass, Supplier<A> factory) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        LoadedServerShip ship = ShipUtils.getLoadedServerShipAtPos(serverLevel, pos);

        return ship != null ? ship.getOrPutAttachment(attachmentClass, factory) : null;
    }


    public static <A extends ShipPhysicsListener> A getOrCreateAttachment(LoadedServerShip ship, Class<A> attachmentClass, Supplier<A> factory, Consumer<A> doWithAttachment) {
        A attachment = ship.getOrPutAttachment(attachmentClass, factory);
        doWithAttachment.accept(attachment);
        return attachment;
    }

    @Nullable
    public static <A extends ShipPhysicsListener> A getOrCreateAttachment(Level level, BlockPos pos, Class<A> attachmentClass, Supplier<A> factory, Consumer<A> doWithAttachment) {
        A attachment = getOrCreateAttachment(level, pos, attachmentClass, factory);
        if (attachment != null) doWithAttachment.accept(attachment);
        return attachment;
    }

    @Nullable
    public static <A extends ShipPhysicsListener> A getOrCreateAttachment(LoadedServerShip ship, Class<A> attachmentClass, Supplier<A> factory, Consumer<A> doWithAttachment, Predicate<A> removalPredicate) {
        A attachment = ship.getOrPutAttachment(attachmentClass, factory);
        doWithAttachment.accept(attachment);
        if (removalPredicate.test(attachment)) {
            ship.removeAttachment(attachmentClass);
            return null;
        }
        return attachment;
    }

    @Nullable
    public static <A extends ShipPhysicsListener> A getOrCreateAttachment(Level level, BlockPos pos, Class<A> attachmentClass, Supplier<A> factory, Consumer<A> doWithAttachment, Predicate<A> removalPredicate) {
        A attachment = getOrCreateAttachment(level, pos, attachmentClass, factory);
        if (attachment != null) {
            doWithAttachment.accept(attachment);
            if (removalPredicate.test(attachment)) {
                removeAttachment(level, pos, attachmentClass);
                return null;
            }
        }
        return attachment;
    }


    @Nullable
    public static <A extends ShipPhysicsListener> A getAttachment(Level level, BlockPos pos, Class<A> attachmentClass) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        LoadedServerShip ship = ShipUtils.getLoadedServerShipAtPos(serverLevel, pos);

        System.out.println("got attachment " + attachmentClass);
        return ship != null ? ship.getAttachment(attachmentClass) : null;
    }

    @Nullable
    public static <A extends ShipPhysicsListener> A getAttachment(LoadedServerShip ship, Class<A> attachmentClass, Consumer<A> doWithAttachment) {
        A attachment = ship.getAttachment(attachmentClass);
        if (attachment != null) doWithAttachment.accept(attachment);
        return attachment;
    }

    @Nullable
    public static <A extends ShipPhysicsListener> A getAttachment(Level level, BlockPos pos, Class<A> attachmentClass, Consumer<A> doWithAttachment) {
        A attachment = getAttachment(level, pos, attachmentClass);
        if (attachment != null) doWithAttachment.accept(attachment);
        return attachment;
    }

    @Nullable
    public static <A extends ShipPhysicsListener> A getAttachment(LoadedServerShip ship, Class<A> attachmentClass, Consumer<A> doWithAttachment, Predicate<A> removalPredicate) {
        A attachment = ship.getAttachment(attachmentClass);
        if (attachment != null) {
            doWithAttachment.accept(attachment);
            if (removalPredicate.test(attachment)) {
                ship.removeAttachment(attachmentClass);
                return null;
            }
        }
        return attachment;
    }

    @Nullable
    public static <A extends ShipPhysicsListener> A getAttachment(Level level, BlockPos pos, Class<A> attachmentClass, Consumer<A> doWithAttachment, Predicate<A> removalPredicate) {
        A attachment = getAttachment(level, pos, attachmentClass);
        if (attachment != null) {
            doWithAttachment.accept(attachment);
            if (removalPredicate.test(attachment)) {
                removeAttachment(level, pos, attachmentClass);
                return null;
            }
        }
        return attachment;
    }

    public static <A extends ShipPhysicsListener> void removeAttachment(Level level, BlockPos pos, Class<A> attachmentClass) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        LoadedServerShip ship = ShipUtils.getLoadedServerShipAtPos(serverLevel, pos);

        if (ship != null) ship.removeAttachment(attachmentClass);
    }
}