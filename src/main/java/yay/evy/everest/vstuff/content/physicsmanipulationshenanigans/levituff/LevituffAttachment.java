package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.core.api.world.PhysLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import yay.evy.everest.vstuff.content.ships.thrust.AttachmentUtils;

import java.util.HashSet;
import java.util.Set;

public final class LevituffAttachment implements ShipPhysicsListener {

    public Set<Vector3d> levituffBlocks = new HashSet<>();

    LevituffAttachment() {}


    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        if (levituffBlocks.isEmpty()) return;

        LevituffBehavior.UNSTOPPABLE_WHIMSY.physTick(physLevel, physShip, levituffBlocks);
    }

    private static Vector3d toV3D(BlockPos pos) {
        return new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public void addBlock(BlockPos pos) {
        levituffBlocks.add(toV3D(pos));
    }

    public void removeBlock(BlockPos pos) {
        levituffBlocks.remove(toV3D(pos));
    }

    public static LevituffAttachment getOrCreateAsAttachment(LoadedServerShip ship) {
        return AttachmentUtils.getOrCreate(
                ship,
                LevituffAttachment.class,
                LevituffAttachment::new
        );
    }

    public static LevituffAttachment get(Level level, BlockPos pos) {
        return AttachmentUtils.get(
                level,
                pos,
                LevituffAttachment.class,
                LevituffAttachment::new
        );
    }
}