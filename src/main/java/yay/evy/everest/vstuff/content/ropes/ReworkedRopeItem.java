package yay.evy.everest.vstuff.content.ropes;


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
import yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.internal.network.NetworkHandler;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public class ReworkedRopeItem extends Item {

    public ReworkedRopeItem(Properties pProperties) {
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

        String blockName = state.getBlock().getName().getString();

        if (!isFoil(heldItem)) {
            if (player.isShiftKeyDown()) return InteractionResult.FAIL;

            if (!IRopeActor.canAttach(state)) {
                firstFailWithMessage(player, clickedPos, "actor_connected", blockName);
                return InteractionResult.SUCCESS;
            }

            firstSelect(serverLevel, clickedPos, state, player, heldItem);
            return InteractionResult.SUCCESS;
        } else if (player.isShiftKeyDown()) {
            player.displayClientMessage(VStuff.translate("rope.reset").withStyle(ChatFormatting.GREEN), true);
            CompoundTag tag = heldItem.getTag().getCompound("first");
            heldItem.setTag(null);
            return InteractionResult.SUCCESS;
        }

        boolean taut = player.isSprinting();
        CompoundTag tag = heldItem.getTag().getCompound("first");
        BlockPos blockPos0 = NbtUtils.readBlockPos(tag.getCompound("blockPos"));

        if (clickedPos.equals(blockPos0)) {
            player.displayClientMessage(VStuff.translate("rope.reset").withStyle(ChatFormatting.GREEN), true);
            heldItem.setTag(null);
            return InteractionResult.SUCCESS;
        }

        RopeConnection.ConnectionInfo info = RopeConnection.tryConnect(
                serverLevel,
                player,
                clickedPos,
                state,
                level.dimension().location().toString(),
                ShipUtils.getLoadedShipIdAtPos(serverLevel, clickedPos),
                heldItem,
                taut
        );

        if (info.valid) {

            Long ship0 = tag.getLong("shipId") == -1 ? null : tag.getLong("shipId");

            ReworkedRope result = ReworkedRope.create(serverLevel,
                    ship0, ShipUtils.getLoadedShipIdAtPos(serverLevel, clickedPos),
                    blockPos0, clickedPos, player, taut);

            player.displayClientMessage(VStuff.translate("rope.created").withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(VStuff.translate(info.message).withStyle(ChatFormatting.RED), true);

            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        heldItem.setTag(null);
        return InteractionResult.SUCCESS;
    }


    private void firstSelect(ServerLevel level, BlockPos clickedPos, BlockState state, Player player, ItemStack heldItem) {
        RopeUtils.SelectType selection = IRopeActor.canActorAttach(state) ? RopeUtils.SelectType.ACTOR : RopeUtils.SelectType.NORMAL;

        String blockName = state.getBlock().getName().getString();

        player.displayClientMessage(VStuff.translate("rope.first", blockName), true);

        CompoundTag tag = heldItem.getOrCreateTagElement("first");

        Long shipId = ShipUtils.getLoadedShipIdAtPos(level, clickedPos);

        tag.putLong("shipId", shipId == null ? -1 : shipId);
        tag.put("blockPos", NbtUtils.writeBlockPos(clickedPos));
        tag.put("worldPos", TagUtils.writeVector3d(RopeUtils.getWorldPos(level, clickedPos, shipId)));
        tag.putString("dim", level.dimension().location().toString());
        tag.putString("type", selection.name());

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.GREEN);
        }
    }


    private void firstFailWithMessage(Player player, BlockPos clickedPos, String message, Object... args) {
        player.displayClientMessage(VStuff.translate("rope." + message, args).withStyle(ChatFormatting.RED), true);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.RED);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("first");
    }

}