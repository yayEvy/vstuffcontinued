package dev.flarelog.vstuff.content.ropes;

import dev.flarelog.vstuff.content.ropes.type.RopeType;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.network.packets.misc.OutlinePacket;
import dev.flarelog.vstuff.content.ropes.util.ILikeRopes;
import dev.flarelog.vstuff.network.VStuffPackets;
import dev.flarelog.vstuff.content.ropes.util.RopeUtil;

public class RopeItem extends Item implements ILikeRopes {

    public RopeItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
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

            player.displayClientMessage(VStuff.translate("message.rope.first", blockName), true);

            CompoundTag tag = heldItem.getOrCreateTagElement("data");

            tag.put("firstClickedPos", NbtUtils.writeBlockPos(clickedPos));
            tag.putString("dim", level.dimension().location().toString());

            if (player instanceof ServerPlayer serverPlayer) {
                VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(clickedPos, OutlinePacket.GREEN));
            }

            return InteractionResult.SUCCESS;

        } else if (player.isShiftKeyDown()) {
            player.displayClientMessage(VStuff.translate("message.rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(heldItem);
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = heldItem.getOrCreateTagElement("data");
        BlockPos firstClickedPos = NbtUtils.readBlockPos(tag.getCompound("firstClickedPos"));

        if (clickedPos.equals(firstClickedPos)) {
            player.displayClientMessage(VStuff.translate("message.rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(heldItem);
            return InteractionResult.SUCCESS;
        }

        RopeFactory.PhysRopeResult ropeResult = RopeFactory.tryCreateNewRope(serverLevel, heldItem, firstClickedPos, clickedPos, player);


        if (ropeResult.valid) {
            player.displayClientMessage(VStuff.translate("message.rope.created").withStyle(ChatFormatting.GREEN), true);

            RopeUtil.playSound(serverLevel, clickedPos, ropeResult.rope.getStyle(serverLevel.registryAccess()).placeSound());

        } else {
            player.displayClientMessage(VStuff.translate(ropeResult.message).withStyle(ChatFormatting.RED), true);

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
        return getNameWithStyle(this, stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isItemFoil(stack);
    }

    @Override
    public ResourceKey<RopeType> getType() {
        return ResourceKey.create(VStuffRegistries.ROPE_TYPE, VStuff.asResource("normal"));
    }
}