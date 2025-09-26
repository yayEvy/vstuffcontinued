package yay.evy.everest.vstuff.index;

import java.util.function.BiFunction;

import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class VStuffShapes {

    public static final VoxelShaper

            ROTATIONAL_THRUSTER = shape(0, 4, 0, 16, 16, 16) // main
            .add(0, 3, 0, 2, 4, 16) // front rim
            .add(14, 3, 0, 16, 4, 16) // front rim
            .add(4, 2, 4, 12, 4, 12) // nozzle (large layer)
            .add(5, 0, 5, 11, 2, 11) // nozzle (small layer)
            .erase(15, 5, 2, 16, 14, 14) // side
            .erase(0, 5, 2, 1, 14, 14) // side
            .erase(2, 4, 0, 14, 16, 1) // top
            .erase(2, 4, 15, 14, 16, 16) // bottom
            .erase(2, 15, 0, 14, 16, 16) // back
            .forDirectional(), // i hate this so much.

        PHYS_PULLEY = shape(0, 0, 0, 16, 16, 16) // main
            .erase(15, 2, 2, 16, 14, 14) // side
            .erase(0, 2, 2, 1, 14, 14) // side
            .erase(3, 15, 0, 13, 16, 16) // back
            .erase(3, 0, 0, 13, 1, 16) // back
            .forDirectional();


    public static Builder shape(VoxelShape shape) {
        return new Builder(shape);
    }

    public static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    public static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    public static class Builder {
        VoxelShape shape;

        public Builder(VoxelShape shape) {
            this.shape = shape;
        }

        public Builder add(VoxelShape shape) {
            this.shape = Shapes.or(this.shape, shape);
            return this;
        }

        public Builder add(double x1, double y1, double z1, double x2, double y2, double z2) {
            return add(cuboid(x1, y1, z1, x2, y2, z2));
        }

        public Builder erase(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.shape =
                    Shapes.join(shape, cuboid(x1, y1, z1, x2, y2, z2), BooleanOp.ONLY_FIRST);
            return this;
        }

        public VoxelShape build() {
            return shape;
        }

        public VoxelShaper build(BiFunction<VoxelShape, Direction, VoxelShaper> factory, Direction direction) {
            return factory.apply(shape, direction);
        }

        public VoxelShaper build(BiFunction<VoxelShape, Axis, VoxelShaper> factory, Axis axis) {
            return factory.apply(shape, axis);
        }

        public VoxelShaper forDirectional(Direction direction) {
            return build(VoxelShaper::forDirectional, direction);
        }

        public VoxelShaper forAxis() {
            return build(VoxelShaper::forAxis, Axis.Y);
        }

        public VoxelShaper forHorizontalAxis() {
            return build(VoxelShaper::forHorizontalAxis, Axis.Z);
        }

        public VoxelShaper forHorizontal(Direction direction) {
            return build(VoxelShaper::forHorizontal, direction);
        }

        public VoxelShaper forDirectional() {
            return forDirectional(Direction.UP);
        }

    }
}

