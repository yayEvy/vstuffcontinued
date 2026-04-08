package yay.evy.everest.vstuff.content.ropes.thrower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.network.PacketDistributor;
import yay.evy.everest.vstuff.content.ropes.packet.OutlinePacket;
import yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffSounds;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

import static yay.evy.everest.vstuff.internal.utility.ShipUtils.getLoadedShipIdAtPos;

public class RopeThrowerItem extends Item {

    public RopeThrowerItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new AbstractProjectileDispenseBehavior() {
        public ItemStack execute(BlockSource source, ItemStack stack) {
            return dispenseRope(source, stack);
        }

        @Override
        protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack) {
            return null;
        }
    };

    public static ItemStack dispenseRope(BlockSource source, ItemStack stack) {
        ServerLevel level = source.getLevel();

        BlockPos dispenserPos = source.getPos();
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);

        Long startShipId = getLoadedShipIdAtPos(level, dispenserPos);

        Position dispensePos = DispenserBlock.getDispensePosition(source);

        RopeThrowerEntity rope = new RopeThrowerEntity(VStuffEntities.ROPE_THROWER.get(), level);
        rope.setPos(dispensePos.x(), dispensePos.y(), dispensePos.z());

        rope.setStartData(
                dispenserPos,
                startShipId,
                level.dimension().location().toString(),
                RopeUtils.ConnectionType.NORMAL,
                null
        );

        rope.shoot(
                direction.getStepX(),
                direction.getStepY(),
                direction.getStepZ(),
                1.1F,
                6.0F
        );

        level.addFreshEntity(rope);
        return stack.split(1);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        BlockPos pos = ctx.getClickedPos();

        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            if (isFoil(stack)) {
                resetStateWithMessage(serverLevel, stack, player, "message.rope.reset");
                if (player instanceof ServerPlayer serverPlayer) {
                    //NetworkHandler.sendOutlineToPlayer(serverPlayer, pos, ClientOutlineHandler.GREEN);
                    VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(pos, OutlinePacket.GREEN));
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        RopeUtils.ConnectionType type = RopeUtils.ConnectionType.NORMAL;

        if (serverLevel.getBlockEntity(pos) instanceof PhysPulleyBlockEntity pulley) {
//            if (!pulley.canAttach()) {
//                sendRopeMessage(player, "pulley_attach_fail");
//                return InteractionResult.FAIL;
//            }

            type = RopeUtils.ConnectionType.PULLEY;
            sendRopeMessage(player, "pulley.first");
        } else {
            sendRopeMessage(player, "rope.first");
        }

        Long shipId = ShipUtils.getShipIdAtPos(serverLevel, pos);
        if (shipId == null) {
            shipId = ShipUtils.getGroundBodyId(serverLevel);
        }

        CompoundTag first = stack.getOrCreateTagElement("data");
        first.put("pos", NbtUtils.writeBlockPos(pos));
        first.putLong("shipId", shipId);
        first.putString("dim", serverLevel.dimension().location().toString());
        first.putString("type", type.name());

        if (player instanceof ServerPlayer serverPlayer) {
            VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OutlinePacket(pos, OutlinePacket.GREEN));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!isFoil(stack)) {
            sendRopeMessage(player, "rope_thrower_not_set");
            return InteractionResultHolder.fail(stack);
        }

        if (isFoil(stack)) {
            CompoundTag tag = stack.getOrCreateTag().getCompound("data");

            RopeUtils.ConnectionType type;
            try {
                type = RopeUtils.ConnectionType.valueOf(tag.getString("type"));
            } catch (Exception e) {
                resetTag(stack);
                return InteractionResultHolder.fail(stack);
            }

            BlockPos firstPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
            Long shipId = tag.getLong("shipId");
            String dim = tag.getString("dim");



            PhysPulleyBlockEntity pulley = null;
            if (type == RopeUtils.ConnectionType.PULLEY &&
                    serverLevel.dimension().location().toString().equals(dim)) {

                if (serverLevel.getBlockEntity(firstPos) instanceof PhysPulleyBlockEntity be) {
                    pulley = be;
                }
            }

            ItemStack thrown = stack.copy();
            thrown.setCount(1);

            RopeThrowerEntity entity = new RopeThrowerEntity(VStuffEntities.ROPE_THROWER.get(), level);
            entity.setOwner(player);
            entity.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

            entity.setItem(thrown);
            entity.setStartData(firstPos, shipId, dim, type, pulley);
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1.0F);

            level.addFreshEntity(entity);

            resetStateWithMessage(serverLevel, stack, player, "rope_thrown");

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    VStuffSounds.ROPE_THROW.get(),
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F
            );

            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    private void resetStateWithMessage(ServerLevel level, ItemStack stack, Player player, String name) {
        sendRopeMessage(player, name);

        resetTag(stack);
    }

    private void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message." + name),
                true
        );
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

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("data");
    }
}

/* sound time, and story time too!

                It all started today,yes today at the time of this commit on december eleventh two thousand and twenty five,
  I was messing around with the rope thrower and thought to myself, " I stole too much damn snowball code, holy shit what am i going to do??
  then I did nothing for two hours as I stared blankly at the wall in front of me. It's my favorite wall, although i havent brought upon
  myself to name it yet. I then messed around with it a bit more and realized where my newfound fear was coming from, the sound.
  before this commit the sound was the normal snowball throwing sound, one of much vapidity in my opinon, so I went to the one place i knew
  would have the best, most fitting sound effects, Youtube.com. I looked for hours upon hours, even though it was the first thing I found
  searching rope throwing sound effect, and found it. all i had to do now was trim it down and fit it to match the throwing action.

  - Bry (12/11/25)
  */

/*
bry what the hell

raven (12/15/25)
 */