package yay.evy.everest.vstuff.content.physics.levituff.attachment;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.core.api.world.PhysLevel;
import yay.evy.everest.vstuff.content.physics.levituff.LevituffBehavior;

import java.util.HashSet;
import java.util.Set;

public abstract sealed class AbstractLevituffAttachment implements ShipPhysicsListener permits RefinedLevituffAttachment, LevituffAttachment {

    public final Set<Vector3d> levituffBlocks;
    protected final LevituffBehavior behavior;

    public AbstractLevituffAttachment(LevituffBehavior behavior) {
        this.behavior = behavior;
        this.levituffBlocks = new HashSet<>();
    }

    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        if (levituffBlocks.isEmpty()) return;

        behavior.physTick(physLevel, physShip, levituffBlocks);
    }

    private static Vector3d toV3D(BlockPos pos) {
        return new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public void addBlock(BlockPos pos) {
        System.out.println(pos);
        levituffBlocks.add(toV3D(pos));
    }

    public void removeBlock(BlockPos pos) {
        System.out.println(pos);
        levituffBlocks.remove(toV3D(pos));
    }

}
