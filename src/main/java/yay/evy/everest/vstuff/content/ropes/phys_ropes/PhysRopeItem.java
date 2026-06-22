package yay.evy.everest.vstuff.content.ropes.phys_ropes;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
import yay.evy.everest.vstuff.content.ropes.util.ILikeRopes;
import yay.evy.everest.vstuff.content.ropes.util.IRopeActor;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;
import yay.evy.everest.vstuff.internal.utility.records.RopePosData;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;

public class PhysRopeItem extends Item implements ILikeRopes {

    public PhysRopeItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos().immutable();
        BlockState state = level.getBlockState(clicked);
        Player player = context.getPlayer();
        ItemStack held = context.getItemInHand();

        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.PASS;
        }

        String blockName = state.getBlock().getName().getString();


        if (!isFoil(held)) {
            if (player.isShiftKeyDown()) return InteractionResult.FAIL;

            if (!IRopeActor.canAttach(state)) {
                player.displayClientMessage(
                        VStuff.translate("message.rope.actor_connected", blockName)
                                .withStyle(ChatFormatting.RED), true);
                if (player instanceof ServerPlayer sp) {
                    VStuffPackets.channel().send(
                            PacketDistributor.PLAYER.with(() -> sp),
                            new OutlinePacket(clicked, OutlinePacket.RED));
                }
                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(VStuff.translate("message.rope.first", blockName), true);

            CompoundTag tag = held.getOrCreateTagElement("data");
            tag.put("firstClickedPos", NbtUtils.writeBlockPos(clicked));
            tag.putString("dim", level.dimension().location().toString());

            if (player instanceof ServerPlayer sp) {
                VStuffPackets.channel().send(
                        PacketDistributor.PLAYER.with(() -> sp),
                        new OutlinePacket(clicked, OutlinePacket.GREEN));
            }

            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            player.displayClientMessage(
                    VStuff.translate("message.rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(held);
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = held.getOrCreateTagElement("data");
        BlockPos firstPos = NbtUtils.readBlockPos(tag.getCompound("firstClickedPos"));

        if (clicked.equals(firstPos)) {
            player.displayClientMessage(
                    VStuff.translate("message.rope.reset").withStyle(ChatFormatting.GREEN), true);
            resetTag(held);
            return InteractionResult.SUCCESS;
        }

        if (!IRopeActor.canAttach(state)) {
            player.displayClientMessage(
                    VStuff.translate("message.rope.actor_connected", blockName).withStyle(ChatFormatting.RED), true);
            if (player instanceof ServerPlayer sp) {
                VStuffPackets.channel().send(
                        PacketDistributor.PLAYER.with(() -> sp),
                        new OutlinePacket(clicked, OutlinePacket.RED));
            }
            return InteractionResult.SUCCESS;
        }

        String originDim = tag.getString("dim");
        if (!originDim.equals(level.dimension().location().toString())) {
            player.displayClientMessage(
                    VStuff.translate("message.rope.interdimensional_fail").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        Long ship0 = ShipUtils.getLoadedShipIdAtPos(serverLevel, firstPos);
        Long ship1 = ShipUtils.getLoadedShipIdAtPos(serverLevel, clicked);

        double dist = RopeUtils.getWorldPos(serverLevel, firstPos, ship0)
                .distance(RopeUtils.getWorldPos(serverLevel, clicked, ship1));

        if (dist > VStuffConfigs.server().ropeMaxLength.get()) {
            player.displayClientMessage(
                    VStuff.translate("message.rope.too_long").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        RopePosData posData0 = RopePosData.create(serverLevel, ship0, firstPos);
        RopePosData posData1 = RopePosData.create(serverLevel, ship1, clicked);

        ResourceKey<RopeStyle> style = RopeStyleManager.get(held.getOrCreateTag());

        PhysRope rope = PhysRopeFactory.createPhysRope(serverLevel, posData0, posData1, style, player);

        if (rope != null) {
            player.displayClientMessage(
                    VStuff.translate("message.rope.created").withStyle(ChatFormatting.GREEN), true);
            RopeUtils.playSound(serverLevel, clicked, rope.getStyle(serverLevel.registryAccess()).placeSound());
        } else {
            player.displayClientMessage(
                    VStuff.translate("message.rope.actor_connected").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        resetTag(held);
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
}