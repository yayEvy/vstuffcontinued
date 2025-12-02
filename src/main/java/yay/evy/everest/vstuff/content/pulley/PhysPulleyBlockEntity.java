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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffItems;

import org.valkyrienskies.mod.api.BlockEntityPhysicsListener;

import java.util.*;

public class PhysPulleyBlockEntity extends KineticBlockEntity implements BlockEntityPhysicsListener {

    private ItemStackHandler ropeInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sendData();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() == Items.STRING || stack.getItem() == Items.LEAD;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    };

    private LazyOptional<IItemHandler> ropeInventoryOptional = LazyOptional.of(() -> ropeInventory);

    public BlockPos targetPos = null;
    public boolean hasTarget = false;
    private Integer constraintId = null;
    private double currentRopeLength = 5.0;
    private double minRopeLength = 0.1;

    private Long shipA = null;
    private Long shipB = null;
    private Vector3d localPosA = null;
    private Vector3d localPosB = null;

    private double consumedRopeLength = 0.0;
    private double baseRopeLength = 1.0;
    private boolean ropeStateInitialized = false;
    boolean isRopeRendering = false;

    boolean isLowering = false;
    private boolean wasCut = false;

    private boolean manualMode = false;

    public static final Map<ServerLevel, Set<PhysPulleyBlockEntity>> ACTIVE_PULLEYS = new WeakHashMap<>();

    public PhysPulleyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public boolean waitingForTarget = false;

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

    }


    @Override
    public @NotNull String getDimension() {
        return "";
    }

    @Override
    public void physTick(@Nullable PhysShip physShip, @NotNull PhysLevel physLevel) {

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

        ItemStack heldItem = player.getItemInHand(hand);
        Item leadItem = VStuffItems.LEAD_CONSTRAINT_ITEM.get();

        if (player.isShiftKeyDown()) {
            waitingForTarget = true;
            setChanged();
            sendData();

            PhysPulleyItem.setWaitingPulley(player, this);


            player.sendSystemMessage(Component.literal("§aPulley manual targeting mode enabled. Next rope click sets target."));

            return InteractionResult.SUCCESS;
        }



        if (heldItem.getItem() == leadItem) {
            if (heldItem.getCount() > 0) {
                return insertRope(player, hand, heldItem);
            } else {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    private InteractionResult insertRope(Player player, InteractionHand hand, ItemStack heldItem) {
        ItemStack remainder = ropeInventory.insertItem(0, heldItem, false);

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
        if (ropeInventory != null) {
            existingStack = ropeInventory.getStackInSlot(0);
        }

        ropeInventory = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() == VStuffItems.LEAD_CONSTRAINT_ITEM.get() ||
                        stack.getItem() == Items.STRING ||
                        stack.getItem() == Items.LEAD;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                sendData();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }
        };

        if (!existingStack.isEmpty()) {
            ropeInventory.setStackInSlot(0, existingStack);
        }

        if (ropeInventoryOptional != null) {
            ropeInventoryOptional.invalidate();
        }
        ropeInventoryOptional = LazyOptional.of(() -> ropeInventory);
    }

    private boolean hasRope() {
        ItemStack ropeStack = ropeInventory.getStackInSlot(0);
        return !ropeStack.isEmpty() && ropeStack.getCount() > 0;
    }

    private double getMaxRopeLength() {
        ItemStack ropeStack = ropeInventory.getStackInSlot(0);
        if (ropeStack.isEmpty()) {
            return baseRopeLength;
        }

        double ropePerItem = getRopePerItem(ropeStack.getItem());
        double availableFromItems = ropeStack.getCount() * ropePerItem;

        return baseRopeLength + availableFromItems;
    }

    private double getRopePerItem(Item item) {
        if (item == VStuffItems.LEAD_CONSTRAINT_ITEM.get()) {
            return 2.0;
        } else if (item == Items.LEAD) {
            return 1.5;
        } else if (item == Items.STRING) {
            return 1.0;
        }
        return 0.0;
    }

    private Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        tooltip.add(Component.literal(" "));

        ItemStack ropeStack = ropeInventory.getStackInSlot(0);
        if (!ropeStack.isEmpty()) {
            double maxLength = getMaxRopeLength() - baseRopeLength;
            tooltip.add(Component.literal("Rope: " + ropeStack.getCount() + "/64 (" +
                            String.format("%.1f", maxLength) + " blocks)")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("No Rope - Right-click with rope item")
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
            if (hasTarget && hasRope()) {
                tooltip.add(Component.literal("Ready - Right-click to create constraint")
                        .withStyle(ChatFormatting.YELLOW));
            } else if (waitingForTarget) {
                tooltip.add(Component.literal("§e§lClick any block to set target")
                        .withStyle(ChatFormatting.YELLOW));
            } else if (!hasRope()){
                tooltip.add(Component.literal("Need rope and a target")
                        .withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.literal("No Target - Shift+Right-click to set")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return true;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("RopeInventory", ropeInventory.serializeNBT());

        if (targetPos != null) {
            tag.putLong("TargetPos", targetPos.asLong());
        }
        tag.putBoolean("HasTarget", hasTarget);
        tag.putBoolean("WaitingForTarget", waitingForTarget);
        tag.putDouble("ConsumedRopeLength", consumedRopeLength);
        tag.putDouble("BaseRopeLength", baseRopeLength);
        tag.putBoolean("RopeStateInitialized", ropeStateInitialized);

        if (constraintId != null) {
            tag.putInt("ConstraintId", constraintId);
        }

        tag.putDouble("CurrentRopeLength", currentRopeLength);
        tag.putDouble("MinRopeLength", minRopeLength);

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
        tag.putBoolean("IsLowering", isLowering);
        tag.putBoolean("IsRopeRendering", isRopeRendering);

        tag.putBoolean("WasCut", wasCut);

    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        ropeInventory.deserializeNBT(tag.getCompound("RopeInventory"));

        if (tag.contains("TargetPos")) {
            targetPos = BlockPos.of(tag.getLong("TargetPos"));
        }


        isLowering = tag.getBoolean("IsLowering");
        isRopeRendering = tag.getBoolean("IsRopeRendering");

        wasCut = tag.getBoolean("WasCut");

        manualMode = tag.getBoolean("ManualMode");


        hasTarget = tag.getBoolean("HasTarget");
        waitingForTarget = tag.getBoolean("WaitingForTarget");

        consumedRopeLength = tag.contains("ConsumedRopeLength") ? tag.getDouble("ConsumedRopeLength") : 0.0;
        baseRopeLength = tag.contains("BaseRopeLength") ? tag.getDouble("BaseRopeLength") : 2.0;
        ropeStateInitialized = tag.getBoolean("RopeStateInitialized");

        if (tag.contains("ConstraintId")) {
            constraintId = tag.getInt("ConstraintId");
        } else {
            constraintId = null;
        }

        currentRopeLength = tag.contains("CurrentRopeLength") ?
                Math.max(tag.getDouble("CurrentRopeLength"), minRopeLength) : minRopeLength;

        minRopeLength = tag.contains("MinRopeLength") ?
                Math.max(tag.getDouble("MinRopeLength"), 0.1) : 0.1;

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
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("IsLowering", isLowering);
        tag.putBoolean("IsRopeRendering", isRopeRendering);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        isLowering = tag.getBoolean("IsLowering");
        isRopeRendering = tag.getBoolean("IsRopeRendering");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ropeInventoryOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        ropeInventoryOptional.invalidate();
    }

    @Override
    public float calculateStressApplied() {
        return 32f;
    }
}