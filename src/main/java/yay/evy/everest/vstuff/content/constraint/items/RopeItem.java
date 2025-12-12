package yay.evy.everest.vstuff.content.constraint.items;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientRopeUtil;
import yay.evy.everest.vstuff.client.NetworkManager;
import yay.evy.everest.vstuff.content.constraint.MasterOfRopes;
import yay.evy.everest.vstuff.content.constraint.ropes.JointlessRope;
import yay.evy.everest.vstuff.content.constraint.ropes.PulleyRope;
import yay.evy.everest.vstuff.content.constraint.ropes.Rope;
import yay.evy.everest.vstuff.content.constraint.ropes.RopeUtils;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.pulley.PulleyAnchorBlockEntity;
import yay.evy.everest.vstuff.events.RopeBreakHandler;
import yay.evy.everest.vstuff.util.GetterUtils;

import java.util.List;

@Mod.EventBusSubscriber(modid = "vstuff")
public class RopeItem extends Item {


    public RopeItem(Properties pProperties) {
        super(pProperties);
    }

    enum ConnectionType {
        NORMAL,
        PULLEY
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && isFoil(heldItem)) { // allow to reset without looking at a block
            resetWithMessage(heldItem, player, "rope_reset");
            return InteractionResultHolder.success(heldItem);
        } else {
            return super.use(level, player, hand);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clickedPos = ctx.getClickedPos().immutable();
        Player player = ctx.getPlayer();
        ItemStack heldItem = ctx.getItemInHand();

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            if (!isFoil(heldItem)) {
                return firstSelect(serverLevel, clickedPos, serverPlayer, ctx.getHand(), heldItem);
            } else {
                return secondSelect(serverLevel, clickedPos, serverPlayer, ctx.getHand(), heldItem);
            }
        }

        else return InteractionResult.PASS;
    }

    public InteractionResult firstSelect(ServerLevel serverLevel, BlockPos firstClickedPos, ServerPlayer player, InteractionHand hand, ItemStack heldItem) {
        ConnectionType type = ConnectionType.NORMAL;
        if (player.isShiftKeyDown()) {
            if (serverLevel.getBlockEntity(firstClickedPos) instanceof PhysPulleyBlockEntity pulleyBE) {
                if (!pulleyBE.acceptsInv()) {
                    resetWithMessage(heldItem, player, "pulley_not_accepting");
                    NetworkManager.sendOutlineToPlayer(player, firstClickedPos, ClientRopeUtil.Colors.RED);
                    return InteractionResult.FAIL;
                }
                resetWithMessage(heldItem, player, "pulley_inserted");
                NetworkManager.sendOutlineToPlayer(player, firstClickedPos, ClientRopeUtil.Colors.YELLOW);
                return pulleyBE.useRopeItem(serverLevel, firstClickedPos, player, hand);
            }
            return InteractionResult.PASS;
        }

        String msg = "rope_first";

        if (serverLevel.getBlockEntity(firstClickedPos) instanceof PhysPulleyBlockEntity pulleyBE) {
            if (!pulleyBE.acceptsManual()) {
                resetWithMessage(heldItem, player, "pulley_not_manual");
                NetworkManager.sendOutlineToPlayer(player, firstClickedPos, ClientRopeUtil.Colors.RED);
                return InteractionResult.FAIL;
            }
            type = ConnectionType.PULLEY;
            msg = "pulley_first";
            pulleyBE.setWaitingLeadConstraintItem(this);
        }
        Long id = RopeUtils.getShipIdAtPos(serverLevel, firstClickedPos);
        Long shipId = id == null ? GetterUtils.getGroundBodyId(serverLevel) : id;

        CompoundTag compoundTag = heldItem.getOrCreateTagElement("connectsFrom");
        compoundTag.put("pos", NbtUtils.writeBlockPos(firstClickedPos));
        compoundTag.putLong("shipId", shipId);
        compoundTag.putString("dimension", serverLevel.dimension().location().toString());
        compoundTag.putString("type", type.name());
        System.out.println(compoundTag);
        sendRopeMessage(player, msg);
        //RopeBreakHandler.addRopeItemTo(heldItem);
        NetworkManager.sendOutlineToPlayer(player, firstClickedPos, ClientRopeUtil.Colors.GREEN);
        return InteractionResult.SUCCESS;
    }

    public InteractionResult secondSelect(ServerLevel serverLevel, BlockPos secondClickedPos, ServerPlayer player, InteractionHand hand, ItemStack heldItem) {
        if (!isFoil(heldItem)) {
            VStuff.LOGGER.warn("[RopeItem] secondSelect should not be called when there is no tag!");
            reset(heldItem);
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.GREEN);
            resetWithMessage(heldItem, player, "rope_reset");
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = heldItem.getTag().getCompound("connectsFrom");
        System.out.println(tag);
        ConnectionType type = ConnectionType.valueOf(tag.getString("type"));

        Long id = RopeUtils.getShipIdAtPos(serverLevel, secondClickedPos);
        Long shipId = id == null ? GetterUtils.getGroundBodyId(serverLevel) : id;

        if (!tag.getString("dimension").equals(serverLevel.dimension().location().toString())) {
            resetWithMessage(heldItem, player, "rope_dimensional_fail");
            NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.RED);
            return InteractionResult.PASS;
        }

        if (!RopeUtils.allowedRopeLength(serverLevel, NbtUtils.readBlockPos(tag.getCompound("pos")), secondClickedPos, tag.getLong("shipId"), shipId)) {
            resetWithMessage(heldItem, player, "rope_too_long");
            NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.RED);
            return InteractionResult.PASS;
        }

        if (type == ConnectionType.PULLEY) {
            if (serverLevel.getBlockEntity(secondClickedPos) instanceof PulleyAnchorBlockEntity pulleyAnchorBE) {
                if (tag.getLong("shipId") == shipId) {
                    NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.RED);
                    resetWithMessage(heldItem, player, "pulley_body_fail");
                    return InteractionResult.PASS;
                }
                PhysPulleyBlockEntity pulleyBE = (PhysPulleyBlockEntity) serverLevel.getBlockEntity(NbtUtils.readBlockPos(tag));
                PulleyRope pulleyRope = PulleyRope.create(serverLevel, player,
                        NbtUtils.readBlockPos(tag.getCompound("pos")),
                        secondClickedPos, tag.getLong("shipId"), shipId);
                MasterOfRopes.ADD(serverLevel, pulleyRope);
                pulleyBE.attachRopeAndAnchor(pulleyRope, pulleyAnchorBE);
                resetWithMessage(heldItem, player, "pulley_attached");
                NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.GREEN);
                return InteractionResult.SUCCESS;
            } else {
                resetWithMessage(heldItem, player, "anchor_fail");
                NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.RED);
                return InteractionResult.PASS;
            }
        } else {
            if (validNormalConnection(serverLevel, secondClickedPos)) {
                if (tag.getLong("shipId") == shipId) {
                    JointlessRope jointlessRope = JointlessRope.create(serverLevel, player,
                            NbtUtils.readBlockPos(tag.getCompound("pos")),
                            secondClickedPos, tag.getLong("shipId"), shipId);
                    MasterOfRopes.ADD(serverLevel, jointlessRope);
                    NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.GREEN);
                    return InteractionResult.SUCCESS;
                }
                Rope rope = Rope.create(serverLevel, player,
                        NbtUtils.readBlockPos(tag.getCompound("pos")),
                        secondClickedPos, tag.getLong("shipId"), shipId);
                MasterOfRopes.ADD(serverLevel, rope);
                NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.GREEN);
                return InteractionResult.SUCCESS;
            } else {
                resetWithMessage(heldItem, player, "rope_invalid");
                NetworkManager.sendOutlineToPlayer(player, secondClickedPos, ClientRopeUtil.Colors.RED);
                return InteractionResult.PASS;
            }
        }
    }

    public boolean validNormalConnection(ServerLevel level, BlockPos pos) {
        List<Class<? extends BlockEntity>> blacklist = List.of(PhysPulleyBlockEntity.class, PulleyAnchorBlockEntity.class);
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return true;
        for (Class<? extends BlockEntity> blockEntityClass : blacklist) {
            if (blockEntityClass == be.getClass()) return false;
        }
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("connectsFrom");
    }


    public void reset(ItemStack heldItem) {
        System.out.println("resetting rope");
        RopeBreakHandler.removeRopeItem(NbtUtils.readBlockPos(heldItem.getTag().getCompound("pos")));
        heldItem.setTag(null);
    }

    private void resetWithMessage(ItemStack heldItem, Player player, String name) {
        sendRopeMessage(player, name);
        reset(heldItem);
    }

    private void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message." + name),
                true
        );
    }
}
