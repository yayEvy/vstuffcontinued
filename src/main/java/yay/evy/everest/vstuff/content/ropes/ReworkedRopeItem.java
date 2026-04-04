package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import yay.evy.everest.vstuff.client.rope.RopeRendererTypes;
import yay.evy.everest.vstuff.content.ropes.type.RopeType;
import yay.evy.everest.vstuff.content.ropes.packet.OutlinePacket;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public class ReworkedRopeItem extends Item {

    public ReworkedRopeItem(Properties properties) {
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
                    //NetworkHandler.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.RED);
                    VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(clickedPos, OutlinePacket.RED));
                }
                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(VStuff.translate("rope.first", blockName), true);

            CompoundTag tag = heldItem.getOrCreateTagElement("data");

            tag.put("firstClickedPos", NbtUtils.writeBlockPos(clickedPos));
            tag.putString("dim", level.dimension().location().toString());

            if (player instanceof ServerPlayer serverPlayer) {
                //NetworkHandler.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.GREEN);
                VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(clickedPos, OutlinePacket.GREEN));
            }

            return InteractionResult.SUCCESS;

        } else if (player.isShiftKeyDown()) {
            player.displayClientMessage(VStuff.translate("rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(heldItem);
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = heldItem.getOrCreateTagElement("data");
        BlockPos firstClickedPos = NbtUtils.readBlockPos(tag.getCompound("firstClickedPos"));

        if (clickedPos.equals(firstClickedPos)) {
            player.displayClientMessage(VStuff.translate("rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(heldItem);
            return InteractionResult.SUCCESS;
        }

        RopeFactory.RopeResult ropeResult = RopeFactory.tryCreateNewRope(serverLevel, heldItem, firstClickedPos, clickedPos, player);


        if (ropeResult.valid()) {
            player.displayClientMessage(VStuff.translate("rope.created").withStyle(ChatFormatting.GREEN), true);

            RopeUtils.playPlaceSound(serverLevel, clickedPos, (ropeResult.rope().type.rendererTypeId().equals(RopeRendererTypes.CHAIN.getId())));

        } else {
            player.displayClientMessage(VStuff.translate(ropeResult.message()).withStyle(ChatFormatting.RED), true);

            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        resetTag(heldItem);

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack))
                .append(" (")
                .append(RopeType.getOrDefault(stack.getOrCreateTag()).name())
                .append(")");
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("data");
    }

    private void resetTag(ItemStack stack) {
        ResourceLocation lastStyle = null;
        if (stack.getTag().contains("style")) {
            lastStyle = TagUtils.readResourceLocation(stack.getTagElement("style"));
        }

        stack.setTag(null);

        if (lastStyle != null) {
            stack.getOrCreateTag().put("style", TagUtils.writeResourceLocation(lastStyle));
        }
        // clears tag then puts the style back if there was one
    }
}