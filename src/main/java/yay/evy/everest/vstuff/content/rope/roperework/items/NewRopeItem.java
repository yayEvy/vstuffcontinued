package yay.evy.everest.vstuff.content.rope.roperework.items;

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
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.core.impl.shadow.Bl;
import org.valkyrienskies.core.impl.shadow.In;
import yay.evy.everest.vstuff.client.ClientOutlineHandler;
import yay.evy.everest.vstuff.content.rope.pulley.PhysPulleyBlock;
import yay.evy.everest.vstuff.content.rope.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.rope.roperework.NewRopeUtils;
import yay.evy.everest.vstuff.foundation.lang.VStuffLang;
import yay.evy.everest.vstuff.foundation.network.NetworkManager;
import yay.evy.everest.vstuff.foundation.utility.PosUtils;

import static yay.evy.everest.vstuff.content.rope.roperework.NewRopeUtils.sendRopeMessage;
import static yay.evy.everest.vstuff.foundation.utility.PosUtils.*;

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
            if (PosUtils.isPulleyAnchor(state)) {
                if (!level.isClientSide) firstFailWithMessage(player, clickedPos, "anchor_first");
                return InteractionResult.SUCCESS;
            }
            if (level.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulley && !pulley.canAttach()) {
                if (!level.isClientSide) firstFailWithMessage(player, clickedPos, "pulley_waiting");
                return InteractionResult.SUCCESS;
            }
            firstSelect(serverLevel, clickedPos, player, heldItem);
            return InteractionResult.SUCCESS;
        }

        boolean taut = Minecraft.getInstance().options.keySprint.isDown();
        NewRopeUtils.RopeInfo info = NewRopeUtils.RopeInfo.tryConnect(level, player, clickedPos, PosUtils.getSafeLoadedShipIdAtPos(serverLevel, clickedPos), state, heldItem, taut);

        if (info.message != null && !level.isClientSide) {
            player.displayClientMessage(VStuffLang.translateDirect(info.message), true);
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        heldItem.setTag(null);
        return InteractionResult.SUCCESS;
    }


    private void firstSelect(ServerLevel level, BlockPos clickedPos, Player player, ItemStack heldItem) {
        SelectType selection = SelectType.NORMAL;
        if (level.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulleyBE && pulleyBE.canAttach()) {
            pulleyBE.setWaiting();
            selection = SelectType.PULLEY;
        }

        if (selection == SelectType.NORMAL) {
            player.displayClientMessage(VStuffLang.translateDirect("rope.first").withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(VStuffLang.translateDirect("rope.pulley_first").withStyle(ChatFormatting.GREEN), true);
        }

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
        player.displayClientMessage(VStuffLang.translateDirect("rope." + message).withStyle(ChatFormatting.RED), true);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkManager.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.RED);
        }
    }

    private void resetWithMessage(Player player, String message) {
        sendRopeMessage(player, message);
        reset();
    }

    private void reset() {

    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("first");
    }

    enum SelectType {
        NORMAL,
        PULLEY
    }
}
