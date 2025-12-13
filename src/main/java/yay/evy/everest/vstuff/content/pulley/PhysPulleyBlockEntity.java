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
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.constraint.LeadConstraintItem;
import yay.evy.everest.vstuff.content.constraint.Rope;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffBlocks;
import yay.evy.everest.vstuff.index.VStuffItems;

import java.util.List;

public class PhysPulleyBlockEntity extends KineticBlockEntity implements BlockEntityPhysicsListener {

    public boolean isExtended = false;
    public boolean hasWaitingRope = false;
    public boolean canAttachManualConstraint = true;

    private ItemStackHandler pulleyInventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sendData();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) { // cannot put items in inventory if the pulley is already extended
            if (slot == 0) return !hasWaitingRope && isExtended && stack.getItem() == VStuffItems.LEAD_CONSTRAINT_ITEM.get();
            else if (slot == 1) return isExtended && stack.getItem() == VStuffBlocks.PULLEY_ANCHOR.asItem();
            else return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private LazyOptional<IItemHandler> pulleyInventoryOpt = LazyOptional.of(() -> pulleyInventory);

    private Integer constraintId = null;
    private Rope attachedRope = null;
    private BlockPos anchorPos = null;
    private double currentRopeLength = 0.0;
    private float minRopeLength = 0.25f;
    private float RPT = 0.1f; // rope per tick, aka how much to extend / retract by each tick
    private float DIR = 1f; // 1 for extend, -1 for retract

    private Long shipA = null;
    private Long shipB = null;
    private Vector3d localPosA = null;
    private Vector3d localPosB = null;

    private boolean ropeStateInitialized = false;

    private boolean manualMode = false;
    private LeadConstraintItem waitingConstraintItem;


    public PhysPulleyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void attachRopeAndAnchor(Rope rope, PulleyAnchorBlockEntity anchor) {
        canAttachManualConstraint = false;
        hasWaitingRope = false;
        isExtended = true;
        anchorPos = anchor.getBlockPos();
        attachedRope = rope;
        currentRopeLength = rope.maxLength;
        constraintId = rope.ID;
        System.out.println(constraintId);
        System.out.println(currentRopeLength);

        clearWaitingLeadConstraintItem();
    }

    public void setWaitingLeadConstraintItem(LeadConstraintItem item) {
        hasWaitingRope = true;
        waitingConstraintItem = item;
        item.waitingPulley = this;
    }

    public void clearWaitingLeadConstraintItem() {
        hasWaitingRope = false;
        waitingConstraintItem.waitingPulley = null;
        waitingConstraintItem = null;
    }

    private void resetSelf() {
        isExtended = false;
        hasWaitingRope = false;
        canAttachManualConstraint = true;
        attachedRope = null;
        anchorPos = null;
        constraintId = null;
    }
    public static PhysPulleyBlockEntity create(BlockPos pos, BlockState state) {
        return new PhysPulleyBlockEntity(VStuffBlockEntities.PHYS_PULLEY_BE.get(), pos, state);
    }

    public void setManualMode(boolean manualMode) { this.manualMode = manualMode; }
    public boolean isManualMode() { return manualMode; }

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
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!isExtended || attachedRope == null) return;

        float speed = getSpeed();
        if (speed == 0) return;

        float ropeDelta = speed * 0.0005f;

        float newLength = (float) attachedRope.maxLength + ropeDelta;
        newLength = Math.max(newLength, minRopeLength);

        attachedRope.setJointLength(serverLevel, newLength);
        currentRopeLength = newLength;
    }


    @Override
    public void setDimension(@NotNull String s) {

    }

    public void sendData() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.getItemInHand(hand).getItem() == VStuffBlocks.PULLEY_ANCHOR.asItem()) {
            return insertAnchor(player, hand);
        }

        return InteractionResult.PASS;
    }

    public InteractionResult insertRope(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        ItemStack remainder = pulleyInventory.insertItem(0, heldItem, false);

        if (remainder.getCount() != heldItem.getCount()) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, remainder);
                canAttachManualConstraint = false;
            }
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
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void initializeRopeInventory() {
        ItemStack existingStack = ItemStack.EMPTY;
        if (pulleyInventory != null) {
            existingStack = pulleyInventory.getStackInSlot(0);
        }

        pulleyInventory = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                sendData();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) { // cannot put items in inventory if the pulley is already extended
                if (slot == 0) return !hasWaitingRope && isExtended && stack.getItem() == VStuffItems.LEAD_CONSTRAINT_ITEM.get();
                else if (slot == 1) return isExtended && stack.getItem() == VStuffBlocks.PULLEY_ANCHOR.asItem();
                else return false;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        if (!existingStack.isEmpty()) {
            pulleyInventory.setStackInSlot(0, existingStack);
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

    private Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
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

        if (constraintId != null) {
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
            } else if (isExtended) {
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

        tag.putBoolean("RopeStateInitialized", ropeStateInitialized);

        if (constraintId != null) {
            tag.putInt("ConstraintId", constraintId);
        }

        tag.putDouble("CurrentRopeLength", currentRopeLength);
        tag.putFloat("MinRopeLength", minRopeLength);

        tag.putBoolean("ManualMode", isManualMode());

        if (shipA != null && shipB != null && localPosA != null && localPosB != null) {
            tag.putLong("ShipA", shipA);
            tag.putLong("ShipB", shipB);
            tag.putDouble("LocalPosAX", localPosA.x);
            tag.putDouble("LocalPosAY", localPosA.y);
            tag.putDouble("LocalPosAZ", localPosA.z);
            tag.putDouble("LocalPosBX", localPosB.x);
            tag.putDouble("LocalPosBY", localPosB.y);
            tag.putDouble("LocalPosBZ", localPosB.z);
        }

        tag.putBoolean("DataSavedProperly", true);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        pulleyInventory.deserializeNBT(tag.getCompound("RopeInventory"));



        manualMode = tag.getBoolean("ManualMode");


        ropeStateInitialized = tag.getBoolean("RopeStateInitialized");

        if (tag.contains("ConstraintId")) {
            constraintId = tag.getInt("ConstraintId");
        } else {
            constraintId = null;
        }

        currentRopeLength = tag.contains("CurrentRopeLength") ?
                Math.max(tag.getDouble("CurrentRopeLength"), minRopeLength) : minRopeLength;

        minRopeLength = tag.contains("MinRopeLength") ?
                Math.max(tag.getFloat("MinRopeLength"), 0.1f) : 0.1f;

        if (tag.contains("ShipA")) {
            shipA = tag.getLong("ShipA");
            shipB = tag.getLong("ShipB");
            localPosA = new Vector3d(
                    tag.getDouble("LocalPosAX"),
                    tag.getDouble("LocalPosAY"),
                    tag.getDouble("LocalPosAZ")
            );
            localPosB = new Vector3d(
                    tag.getDouble("LocalPosBX"),
                    tag.getDouble("LocalPosBY"),
                    tag.getDouble("LocalPosBZ")
            );
        }

    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("is_extended", isExtended);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        isExtended = tag.getBoolean("is_extended");
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