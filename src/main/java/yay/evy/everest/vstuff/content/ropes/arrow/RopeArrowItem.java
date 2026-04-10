package yay.evy.everest.vstuff.content.ropes.arrow;//package yay.evy.everest.vstuff.content.ropes.arrow;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.ILikeRopes;
import yay.evy.everest.vstuff.content.ropes.IRopeActor;
import yay.evy.everest.vstuff.content.ropes.packet.OutlinePacket;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public class RopeArrowItem extends ArrowItem implements ILikeRopes {


    public RopeArrowItem(Properties properties) { super(properties);}

    @Override
    public @NotNull AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        RopeArrowEntity arrow = new RopeArrowEntity(VStuffEntities.ROPE_ARROW.get(), shooter, level);

        if (stack.hasTag() && stack.getTag().contains("data")) {
            CompoundTag data = stack.getTagElement("data");

            if (!data.contains("firstPos") || !data.contains("firstDim")) {
                arrow.setInvalid();
            } else {
                arrow.setFirstPos(NbtUtils.readBlockPos(data.getCompound("firstPos")));
                arrow.setFirstDim(data.getString("firstDim"));
            }

            if (stack.getTag().contains("style")) {
                ResourceLocation styleId = TagUtils.readResourceLocation(stack.getTagElement("style"));
                arrow.setStyle(styleId);
            }
        } else arrow.setInvalid();

        return arrow;
    }

    @Override
    public InteractionResult useOn(@NotNull UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clickedPos = ctx.getClickedPos().immutable();
        Player player = ctx.getPlayer();
        ItemStack heldItem = ctx.getItemInHand();

        if (!(level instanceof ServerLevel) || player == null) {
            return InteractionResult.PASS;
        }

        if (!isFoil(heldItem)) {
            if (player.isShiftKeyDown()) return InteractionResult.FAIL;

            return selection(level, clickedPos, heldItem, player);
        } else if (player.isShiftKeyDown()) {
            player.displayClientMessage(VStuff.translate("message.rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(heldItem);
            return InteractionResult.SUCCESS;
        } else if (isFoil(heldItem)) {
            return selection(level, clickedPos, heldItem, player);
        }

        return InteractionResult.FAIL;
    }

    private InteractionResult selection(Level level, BlockPos clickedPos, ItemStack heldItem, Player player) {
        BlockState state = level.getBlockState(clickedPos);

        String blockName = state.getBlock().getName().getString();

        if (!IRopeActor.canAttach(state)) {
            player.displayClientMessage(VStuff.translate("message.rope.actor_connected", blockName).withStyle(ChatFormatting.RED), true);
            if (player instanceof ServerPlayer serverPlayer) {
                VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(clickedPos, OutlinePacket.RED));
            }
            return InteractionResult.FAIL;
        }

        player.displayClientMessage(VStuff.translate("message.rope.first", blockName), true);

        CompoundTag tag = heldItem.getOrCreateTagElement("data");

        tag.put("firstPos", NbtUtils.writeBlockPos(clickedPos));
        tag.putString("firstDim", level.dimension().location().toString());

        if (player instanceof ServerPlayer serverPlayer) {
            VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(clickedPos, OutlinePacket.GREEN));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isItemFoil(stack);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return getNameWithStyle(this, stack);
    }

}
