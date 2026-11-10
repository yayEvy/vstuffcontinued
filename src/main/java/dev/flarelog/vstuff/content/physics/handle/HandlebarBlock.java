package dev.flarelog.vstuff.content.physics.handle;

import com.simibubi.create.foundation.block.IBE;
import dev.flarelog.vstuff.index.VStuffBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.flarelog.vstuff.index.VStuffShapes;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.jetbrains.annotations.NotNull;


public class HandlebarBlock extends DirectionalBlock implements IBE<HandlebarBlockEntity> {

    private final DyeColor color;

    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");

    public HandlebarBlock(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROTATED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Vec3 loc = context.getClickLocation();

        double x = loc.x - pos.getX() - 0.5;
        double y = loc.y - pos.getY() - 0.5;
        double z = loc.z - pos.getZ() - 0.5;

        boolean rotated;

        if (face.getAxis() == Direction.Axis.Y) {
            rotated = context.getHorizontalDirection().getAxis() == Direction.Axis.X;
        } else if (face.getAxis() == Direction.Axis.X) {
            rotated = Math.abs(y) > Math.abs(z);
        } else {
            rotated = Math.abs(y) > Math.abs(x);
        }

        return this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(ROTATED, rotated);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        CompoundTag stateTag = new CompoundTag();
        stack.getOrCreateTag().put("BlockStateTag", stateTag);
        return stack;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        Direction facing = state.getValue(FACING);
        boolean rotated = state.getValue(ROTATED);

        if (facing.getAxis().isVertical()) {
            if (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90) {
                rotated = !rotated;
            }
        }

        return state
                .setValue(FACING, rot.rotate(facing))
                .setValue(ROTATED, rotated);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public Class<HandlebarBlockEntity> getBlockEntityClass() {
        return HandlebarBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HandlebarBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.HANDLEBAR_BE.get();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(ROTATED)
                ? VStuffShapes.HANDLE_ROTATED
                : VStuffShapes.HANDLE).get(state.getValue(FACING));
    }
}