package yay.evy.everest.vstuff.content.rope.roperework.items;

import kotlin.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientOutlineHandler;
import yay.evy.everest.vstuff.content.rope.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.rope.roperework.Rope;
import yay.evy.everest.vstuff.content.rope.roperework.RopeUtil;
import yay.evy.everest.vstuff.foundation.network.NetworkManager;
import yay.evy.everest.vstuff.foundation.utility.PosUtils;


public class RopeItem extends Item {
    public RopeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos().immutable();
        BlockState state = level.getBlockState(clickedPos);
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();


        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.PASS;
        }

        String blockName = level.getBlockState(clickedPos).getBlock().getName().getString();

        if (!isFoil(heldItem)) {
            if (player.isShiftKeyDown()) return InteractionResult.FAIL;
            if (PosUtils.isPulleyAnchor(state)) {
                if (!level.isClientSide) firstFailWithMessage(player, clickedPos, "invalid_first", blockName);
                return InteractionResult.SUCCESS;
            }
            if (level.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulley && !pulley.canAttach()) {
                if (!level.isClientSide) firstFailWithMessage(player, clickedPos, "invalid_first", blockName);
                return InteractionResult.SUCCESS;
            }
            firstSelect(serverLevel, clickedPos, player, heldItem);
            return InteractionResult.SUCCESS;
        } else if (player.isShiftKeyDown()) {
            player.displayClientMessage(VStuff.translate("rope.reset").withStyle(ChatFormatting.GREEN), true);
            CompoundTag tag = heldItem.getTag().getCompound("first");
            if (serverLevel.getBlockEntity(NbtUtils.readBlockPos(tag.getCompound("pos"))) instanceof PhysPulleyBlockEntity pulleyBE) {
                pulleyBE.resetSelf();
            }
            heldItem.setTag(null);
            return InteractionResult.SUCCESS;
        }

        boolean taut = Minecraft.getInstance().options.keySprint.isDown();
        CompoundTag tag = heldItem.getTag().getCompound("first");
        BlockPos blockPos0 = NbtUtils.readBlockPos(tag.getCompound("pos"));

        if (clickedPos.equals(blockPos0)) {
            player.displayClientMessage(VStuff.translate("rope.reset").withStyle(ChatFormatting.GREEN), true);
            if (serverLevel.getBlockEntity(blockPos0) instanceof PhysPulleyBlockEntity pulleyBE) {
                pulleyBE.resetSelf();
            }
            heldItem.setTag(null);
            return InteractionResult.SUCCESS;
        }

        Pair<Rope, String> result = Rope.create(serverLevel,
                tag.getLong("shipId"), PosUtils.getLoadedShipIdAtPos(serverLevel, clickedPos),
                blockPos0, clickedPos, player, taut);

        if (result.component1() == null) {
            player.displayClientMessage(VStuff.translate(result.component2()).withStyle(ChatFormatting.RED), true);
            if (serverLevel.getBlockEntity(NbtUtils.readBlockPos(tag.getCompound("pos"))) instanceof PhysPulleyBlockEntity pulleyBE) {
                pulleyBE.resetSelf();
            }
            heldItem.setTag(null);
            return InteractionResult.SUCCESS;

        }

        if (result.component2() != null && !level.isClientSide) {
            player.displayClientMessage(VStuff.translate(result.component2()).withStyle(ChatFormatting.GREEN), true);
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        heldItem.setTag(null);
        return InteractionResult.SUCCESS;
    }


    private void firstSelect(ServerLevel level, BlockPos clickedPos, Player player, ItemStack heldItem) {
        RopeUtil.SelectType selection = RopeUtil.SelectType.NORMAL;
        if (level.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulleyBE && pulleyBE.canAttach()) {
            pulleyBE.setWaiting();
            selection = RopeUtil.SelectType.PULLEY;
        }

        String blockName = level.getBlockState(clickedPos).getBlock().getName().getString();

        player.displayClientMessage(VStuff.translate("rope.first", blockName), true);

        CompoundTag tag = heldItem.getOrCreateTagElement("first");

        tag.putLong("shipId", PosUtils.getSafeLoadedShipIdAtPos(level, clickedPos));
        tag.put("pos", NbtUtils.writeBlockPos(clickedPos));
        tag.putString("dim", level.dimension().location().toString());
        tag.putString("type", selection.name());

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkManager.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.GREEN);
        }
    }


    private void firstFailWithMessage(Player player, BlockPos clickedPos, String message, Object... args) {
        player.displayClientMessage(VStuff.translate("rope." + message, args).withStyle(ChatFormatting.RED), true);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkManager.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.RED);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("first");
    }
}
