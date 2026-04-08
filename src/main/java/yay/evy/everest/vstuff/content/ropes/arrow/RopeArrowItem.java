package yay.evy.everest.vstuff.content.ropes.arrow;//package yay.evy.everest.vstuff.content.ropes.arrow;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.packet.OutlinePacket;
import yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.utility.GTPAUtils;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

import java.util.GregorianCalendar;
import java.util.function.Supplier;

public class RopeArrowItem extends ArrowItem {


    public RopeArrowItem(Properties properties) { super(properties);}

    private static BlockPos clickedPos;

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {

        return new RopeArrowEntity(VStuffEntities.ROPE_ARROW.get(), shooter, level);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctext) {
        if (ctext.getPlayer() instanceof ServerPlayer serverPlayer) {

            ItemStack stack = ctext.getItemInHand();

            if (serverPlayer.isShiftKeyDown()) {
                if (isFoil(stack)) {
                    resetStateWithMessage((ServerLevel) ctext.getLevel(), stack, serverPlayer, "rope_reset");
                    VStuffPackets.channel().send(
                            PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new OutlinePacket(ctext.getClickedPos(), OutlinePacket.RED)
                    );
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }

            clickedPos = ctext.getClickedPos();

            CompoundTag tag = stack.getOrCreateTagElement("data");
            tag.put("pos", NbtUtils.writeBlockPos(clickedPos));
            tag.putString("dim", ctext.getLevel().dimension().location().toString());

            VStuffPackets.channel().send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OutlinePacket(clickedPos, OutlinePacket.GREEN)
            );

            sendRopeMessage(ctext.getPlayer(), "rope_first");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public static BlockPos getClickedPos(){
     return clickedPos;
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
        return bow.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0 ;
    }

    private void resetStateWithMessage(ServerLevel level, ItemStack stack, Player player, String name) {
        sendRopeMessage(player, name);

        resetState(level, stack);
    }

    private void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message." + name),
                true
        );
    }


    private void resetState(ServerLevel level, ItemStack stack) {
        if (!isFoil(stack)) return;

        CompoundTag tag = stack.getTagElement("data");
        if (tag != null) {

            if (tag.contains("type")) {
                try {
                    RopeUtils.ConnectionType type =
                            RopeUtils.ConnectionType.valueOf(tag.getString("type"));

                    if (type == RopeUtils.ConnectionType.PULLEY && tag.contains("pos")) {
                        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
                        if (level.getBlockEntity(pos) instanceof PhysPulleyBlockEntity pulleyBE) {
                            // ion know
                        }
                    }

                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        stack.setTag(null);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("data");
    }

}
