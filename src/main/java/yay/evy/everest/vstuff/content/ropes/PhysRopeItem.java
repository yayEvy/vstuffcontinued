package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.ChatFormatting;
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
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.packet.OutlinePacket;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

public class PhysRopeItem extends Item {

    public PhysRopeItem(Properties properties) {
        super(properties);
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
                player.displayClientMessage(VStuff.translate("rope.actor_connected", blockName).withStyle(ChatFormatting.RED), true);
                if (player instanceof ServerPlayer serverPlayer) {
                    VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(clickedPos, OutlinePacket.RED));
                }
                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(VStuff.translate("rope.first", blockName), true);

            CompoundTag tag = heldItem.getOrCreateTagElement("data");
            tag.put("firstClickedPos", NbtUtils.writeBlockPos(clickedPos));
            tag.putString("dim", level.dimension().location().toString());

            if (player instanceof ServerPlayer serverPlayer) {
                VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(clickedPos, OutlinePacket.GREEN));
            }

            return InteractionResult.SUCCESS;

        } else if (player.isShiftKeyDown()) {
            player.displayClientMessage(VStuff.translate("rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(heldItem);
            return InteractionResult.PASS;
        }

        CompoundTag tag = heldItem.getOrCreateTagElement("data");
        BlockPos firstClickedPos = NbtUtils.readBlockPos(tag.getCompound("firstClickedPos"));

        if (clickedPos.equals(firstClickedPos)) {
            player.displayClientMessage(VStuff.translate("rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(heldItem);
            return InteractionResult.SUCCESS;
        }

        if (!IRopeActor.canAttach(state)) {
            player.displayClientMessage(VStuff.translate("rope.actor_connected", blockName).withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        String originDimension = tag.getString("dim");
        if (!originDimension.equals(level.dimension().location().toString())) {
            player.displayClientMessage(VStuff.translate("rope.dimension").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        Long ship0 = ShipUtils.getLoadedShipIdAtPos(serverLevel, firstClickedPos);
        Long ship1 = ShipUtils.getLoadedShipIdAtPos(serverLevel, clickedPos);

        PhysRopeConstraint constraint = PhysRopeFactory.createPhysRope(serverLevel, firstClickedPos, clickedPos, ship0, ship1, player);

        if (constraint != null) {
            player.displayClientMessage(VStuff.translate("rope.created").withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(VStuff.translate("rope.failed").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        resetTag(heldItem);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("data");
    }

    private void resetTag(ItemStack stack) {
        stack.setTag(null);
    }
}