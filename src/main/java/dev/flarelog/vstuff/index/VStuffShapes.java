package dev.flarelog.vstuff.index;

import java.util.function.BiFunction;


import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class VStuffShapes {

    public static final VoxelShaper

        MECHANICAL_THRUSTER = shape(0,0,0,16,16,16) // ill do these later im just tired
                .forDirectional(),




    NAIL = shape(3.0, 0.075, 4.0, 13.0, 2.5, 12.0)
            .add(2.68, 0.025, 2.135, 4.68, 2.975, 8.635)
            .add(2.68, 0.025, 7.365, 4.68, 2.975, 13.865)
            .add(11.32, 0.025, 2.135, 13.32, 2.975, 8.635)
            .add(11.32, 0.025, 7.365, 13.32, 2.975, 13.865)
            .add(4.0, 0.0, 2.0, 12.0, 3.0, 4.0)
            .add(4.0, 0.0, 12.0, 12.0, 3.0, 14.0)
            .forDirectional(),

        PHYS_PULLEY = shape(0, 0, 0, 16, 16, 16)
                .forDirectional(),

        PULLEY_ANCHOR = shape(4, 0, 4, 12, 1, 12)
                .add(3, 1, 3, 13, 2, 13)
                .add(5, 2, 5, 11, 3, 11)
                .forDirectional(Direction.UP);


    public static final VoxelShaper HANDLE = shape(1, 0, 6.5, 4, 2, 9.5)
            .add(8, 3, 7, 14.5, 5, 9)
            .add(12.5, 2, 7, 14.5, 3, 9)
            .add(12, 0, 6.5, 15, 2, 9.5)
            .add(1.5, 2, 7, 3.5, 3, 9)
            .add(1.5, 3, 7, 8, 5, 9)
            .forDirectional();

    private static Builder shape(VoxelShape shape) {
        return new Builder(shape);
    }


    private static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    private static class Builder {
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

