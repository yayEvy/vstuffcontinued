package yay.evy.everest.vstuff.content.pulley;


import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.mod.api.BlockEntityPhysicsListener;


import yay.evy.everest.vstuff.content.constraint.MasterOfRopes;
import yay.evy.everest.vstuff.content.constraint.items.RopeItem;
import yay.evy.everest.vstuff.content.constraint.ropes.PulleyRope;
import yay.evy.everest.vstuff.content.constraint.ropes.RopeUtils;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffBlocks;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.List;

import static yay.evy.everest.vstuff.content.constraint.ropes.RopeUtils.*;

public class PhysPulleyBlockEntity extends KineticBlockEntity implements BlockEntityPhysicsListener {

    public int physticks = 0;

    public enum PulleyState {
        /**
         * the pulley has nothing in the inventory, is not extended, and does not have a waiting rope
         */
        OPEN,
        /**
         * the pulley has one of the two needed items in inventory
         */
        WAITING_INV,
        /**
         * a RopeItem has the pulley as the firstClickedPos
         */
        WAITING_MANUAL_ANCHOR,
        /**
         * the pulley inventory has both the rope and anchor
         */
        FULL_INV,
        /**
         * the pulley is extended, either by inventory or manual
         */
        EXTENDED
    }

    private ItemStackHandler pulleyInventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sendData();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) { // cannot put items in inventory if the pulley is already extended
            if (slot == 0) return acceptsInv() && stack.getItem() == VStuffItems.ROPE_ITEM.get();
            else if (slot == 1) return acceptsInv() && stack.getItem() == VStuffBlocks.PULLEY_ANCHOR.asItem();
            else return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private LazyOptional<IItemHandler> pulleyInventoryOpt = LazyOptional.of(() -> pulleyInventory);

    private Integer ropeId = null;
    private RopeStyles.RopeStyle attachedStyle = RopeStyles.fromString("normal");
    private PulleyRope attachedRope = null;
    private BlockPos anchorPos = null;
    private double currentRopeLength = 0.0;
    private float minRopeLength = 0.25f;
    private float RPT = 0.1f; // rope per tick, aka how much to extend / retract by each tick
    private float DIR = 1f; // 1 for extend, -1 for retract
    public PulleyState state = PulleyState.OPEN;

    private Long shipA = null;
    private Long shipB = null;
    private Vector3d localPosA = null;
    private Vector3d localPosB = null;

    private boolean ropeStateInitialized = false;

    private RopeItem waitingConstraintItem;

    public boolean acceptsInv() {
        return (state == PulleyState.OPEN || state == PulleyState.WAITING_INV);
    }

    public boolean acceptsManual() {
        return state == PulleyState.OPEN;
    }

    public PhysPulleyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void attachRopeAndAnchor(PulleyRope rope, PulleyAnchorBlockEntity anchor) {
        this.state = PulleyState.EXTENDED;
        anchorPos = anchor.getBlockPos();
        attachedRope = rope;
        attachedStyle = rope.style;
        currentRopeLength = rope.maxLength;
        ropeId = rope.ID;
        System.out.println(ropeId);
        System.out.println(currentRopeLength);

        clearWaitingLeadConstraintItem();
    }

    public void setWaitingLeadConstraintItem(RopeItem item) {
        waitingConstraintItem = item;
        this.state = PulleyState.WAITING_MANUAL_ANCHOR;
    }

    public void clearWaitingLeadConstraintItem() {
        if (waitingConstraintItem == null) return;

        waitingConstraintItem = null;
    }

    private void resetSelf() {
        attachedRope = null;
        anchorPos = null;
        ropeId = null;
        this.state = PulleyState.OPEN;
    }
    public static PhysPulleyBlockEntity create(BlockPos pos, BlockState state) {
        return new PhysPulleyBlockEntity(VStuffBlockEntities.PHYS_PULLEY_BE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

    }

    @Override
    public void onLoad() {
        super.onLoad();

        initializeRopeInventory();
        ropeStateInitialized = true;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) return;

        if (anchorPos != null && !(level.getBlockEntity(anchorPos) instanceof PulleyAnchorBlockEntity)) {
            resetSelf();
        }

        DIR = getSpeed() / Math.abs(getSpeed());
    }


    @Override
    public @NotNull String getDimension() {
        return "";
    }

    @Override
    public void physTick(@Nullable PhysShip physShip, @NotNull PhysLevel physLevel) {
        physticks++;
        if (physticks == 20) {
            System.out.println("phystick");
            physticks = 0;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (state == PulleyState.EXTENDED && getSpeed() != 0f) {
            if (attachedRope.maxLength > minRopeLength) {
                attachedRope.shiftJointLength(serverLevel, Math.max(0.001f * getSpeed(), minRopeLength));
            }
            currentRopeLength = attachedRope.maxLength;
        }
    }

    @Override
    public void setDimension(@NotNull String s) {

    }

    public void sendData() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public InteractionResult useRopeItem(ServerLevel level, BlockPos pos, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() == VStuffItems.ROPE_ITEM.asItem()) {
            this.attachedStyle = RopeStyleHandlerServer.getStyle(player.getUUID());
            return insertRope(player, hand);
        }

        if (hasAnchor() && hasRope()) {
            this.state = PulleyState.FULL_INV;
        }

        return InteractionResult.PASS;
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.getItemInHand(hand).getItem() == VStuffBlocks.PULLEY_ANCHOR.asItem()) {
            return insertAnchor(player, hand);
        }

        if (hasAnchor() && hasRope()) {
            this.state = PulleyState.FULL_INV;
        }

        return InteractionResult.PASS;
    }

    public InteractionResult insertRope(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        ItemStack remainder = pulleyInventory.insertItem(0, heldItem, false);

        if (remainder.getCount() != heldItem.getCount()) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, remainder);
            }
            this.state = PulleyState.WAITING_INV;
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private InteractionResult insertAnchor(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        ItemStack remainder = pulleyInventory.insertItem(1, heldItem, false);

        if (remainder.getCount() != heldItem.getCount()) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, remainder);
            }
            this.state = PulleyState.WAITING_INV;
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void initializeRopeInventory() {
        ItemStack existingRope = ItemStack.EMPTY;
        ItemStack existingAnchor = ItemStack.EMPTY;
        if (pulleyInventory != null) {
            existingRope = pulleyInventory.getStackInSlot(0);
            existingAnchor = pulleyInventory.getStackInSlot(1);
        }

        pulleyInventory = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                sendData();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) { // cannot put items in inventory if the pulley is already extended
                if (slot == 0) return acceptsInv() && stack.getItem() == VStuffItems.ROPE_ITEM.get();
                else if (slot == 1) return acceptsInv() && stack.getItem() == VStuffBlocks.PULLEY_ANCHOR.asItem();
                else return false;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        if (!existingRope.isEmpty()) {
            pulleyInventory.setStackInSlot(0, existingRope);
        }

        if (!existingAnchor.isEmpty()) {
            pulleyInventory.setStackInSlot(0, existingAnchor);
        }

        if (pulleyInventoryOpt != null) {
            pulleyInventoryOpt.invalidate();
        }
        pulleyInventoryOpt = LazyOptional.of(() -> pulleyInventory);
    }

    private boolean hasRope() {
        ItemStack ropeStack = pulleyInventory.getStackInSlot(0);
        return !ropeStack.isEmpty();
    }

    private boolean hasAnchor() {
        ItemStack anchorStack = pulleyInventory.getStackInSlot(1);
        return !anchorStack.isEmpty();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        tooltip.add(Component.literal(" "));

        if (hasRope()) {
            tooltip.add(Component.literal("Rope: Yes")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("Rope: No")
                    .withStyle(ChatFormatting.RED));
        }

        if (hasAnchor()) {
            tooltip.add(Component.literal("Anchor: Yes")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("Anchor: No")
                    .withStyle(ChatFormatting.RED));
        }

        if (ropeId != null) {
            tooltip.add(Component.literal("Length: " + String.format("%.1f", currentRopeLength) + " blocks")
                    .withStyle(ChatFormatting.BLUE));
            float speed = getSpeed();
            if (Math.abs(speed) > 4) {
                String direction = speed > 0 ? "Extending" : "Retracting";
                tooltip.add(Component.literal("Status: " + direction + " (" + String.format("%.1f", speed))
                        .withStyle(ChatFormatting.GREEN));

            } else {
                tooltip.add(Component.literal("Status: Idle")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            if (hasAnchor() && hasRope()) {
                tooltip.add(Component.literal("Ready - Pulley is able to extend")
                        .withStyle(ChatFormatting.YELLOW));
            } else if (state == PulleyState.EXTENDED) {
                tooltip.add(Component.literal("Extended - ")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        return true;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("pulleyInventory", pulleyInventory.serializeNBT());

        tag.putBoolean("ropeStateInit", ropeStateInitialized);

        if (ropeId != null) {
            tag.putInt("ropeId", ropeId);
        }

        if (anchorPos != null) {
            putBlockPos("anchorPos", anchorPos, tag);
        }

        tag.putDouble("currentLength", currentRopeLength);
        tag.putFloat("minLength", minRopeLength);

        if (shipA != null && shipB != null && localPosA != null && localPosB != null) {
            tag.putLong("ship0", shipA);
            tag.putLong("ship1", shipB);
            putVector3d("localPos0", localPosA, tag);
            putVector3d("localPos1", localPosB, tag);
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        pulleyInventory.deserializeNBT(tag.getCompound("pulleyInventory"));

        ropeStateInitialized = tag.getBoolean("ropeStateInit");

        if (tag.contains("ropeId")) {
            ropeId = tag.getInt("ropeId");
            attachedRope = (PulleyRope) MasterOfRopes.GET(ropeId);
        } else {
            ropeId = null;
            attachedRope = null;
        }

        if (tag.contains("anchorPos_x")) {
            anchorPos = RopeUtils.getBlockPos("anchorPos", tag);
        }

        currentRopeLength = tag.contains("currentLength") ?
                Math.max(tag.getDouble("currentLength"), minRopeLength) : minRopeLength;

        minRopeLength = tag.contains("minLength") ?
                Math.max(tag.getFloat("minLength"), 0.1f) : 0.1f;

        if (tag.contains("ship0")) {
            shipA = tag.getLong("ship0");
            shipB = tag.getLong("ship1");
            localPosA = getVector3d("localPos0", tag);
            localPosB = getVector3d("localPos1", tag);
        }

    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("state", this.state.name());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.state = PulleyState.valueOf(tag.getString("state"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return pulleyInventoryOpt.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        pulleyInventoryOpt.invalidate();
    }

    @Override
    public float calculateStressApplied() {
        return 32f;
    }
}