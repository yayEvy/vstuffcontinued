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
import yay.evy.everest.vstuff.content.rope.roperework.NewRope;
import yay.evy.everest.vstuff.content.rope.roperework.NewRopeUtils;
import yay.evy.everest.vstuff.foundation.network.NetworkManager;
import yay.evy.everest.vstuff.foundation.utility.PosUtils;


public class NewRopeItem extends Item {
    public NewRopeItem(Properties pProperties) {
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

        if (!isFoil(heldItem)) {
            if (player.isShiftKeyDown()) return InteractionResult.FAIL;
            if (PosUtils.isPulleyAnchor(state)) {
                if (!level.isClientSide) firstFailWithMessage(player, clickedPos, "invalid_first");
                return InteractionResult.SUCCESS;
            }
            if (level.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulley && !pulley.canAttach()) {
                if (!level.isClientSide) firstFailWithMessage(player, clickedPos, "invalid_second");
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
        Pair<NewRope, String> result = NewRope.create(serverLevel,
                tag.getLong("shipId"), PosUtils.getLoadedShipIdAtPos(serverLevel, clickedPos),
                NbtUtils.readBlockPos(tag.getCompound("pos")), clickedPos,
                player, taut);

        if (result.component2() != null && !level.isClientSide) {
            player.displayClientMessage(VStuff.translate(result.component2()), true);
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        heldItem.setTag(null);
        return InteractionResult.SUCCESS;
    }


    private void firstSelect(ServerLevel level, BlockPos clickedPos, Player player, ItemStack heldItem) {
        NewRopeUtils.SelectType selection = NewRopeUtils.SelectType.NORMAL;
        if (level.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulleyBE && pulleyBE.canAttach()) {
            pulleyBE.setWaiting();
            selection = NewRopeUtils.SelectType.PULLEY;
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


    private void firstFailWithMessage(Player player, BlockPos clickedPos, String message) {
        player.displayClientMessage(VStuff.translate("rope." + message).withStyle(ChatFormatting.RED), true);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkManager.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.RED);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("first");
    }
}
