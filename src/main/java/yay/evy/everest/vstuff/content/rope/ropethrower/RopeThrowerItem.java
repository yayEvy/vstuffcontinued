package yay.evy.everest.vstuff.content.rope.ropethrower;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.client.ClientOutlineHandler;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.rope.ropes.RopeUtil;
import yay.evy.everest.vstuff.content.rope.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffSounds;

import static yay.evy.everest.vstuff.content.rope.ropes.RopeUtil.getShipIdAtPos;

public class RopeThrowerItem extends Item {

    public RopeThrowerItem(Properties properties) {
        super(properties);
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
                resetStateWithMessage(serverLevel, stack, player, "rope_reset");
                NetworkHandler.sendOutline(pos, ClientOutlineHandler.GREEN);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        RopeUtil.ConnectionType type = RopeUtil.ConnectionType.NORMAL;

        if (serverLevel.getBlockEntity(pos) instanceof PhysPulleyBlockEntity pulley) {
            if (!pulley.canAttach()) {
                sendRopeMessage(player, "pulley_attach_fail");
                return InteractionResult.FAIL;
            }

            pulley.setWaiting();
            type = RopeUtil.ConnectionType.PULLEY;
            sendRopeMessage(player, "pulley_first");
        } else {
            sendRopeMessage(player, "rope_first");
        }

        Long shipId = getShipIdAtPos(serverLevel, pos);
        if (shipId == null) {
            shipId = RopeUtil.getGroundBodyId(serverLevel);
        }

        CompoundTag first = stack.getOrCreateTagElement("first");
        first.put("pos", NbtUtils.writeBlockPos(pos));
        first.putLong("shipId", shipId);
        first.putString("dim", serverLevel.dimension().location().toString());
        first.putString("type", type.name());

        NetworkHandler.sendOutline(pos, ClientOutlineHandler.GREEN);
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
            CompoundTag tag = stack.getTag().getCompound("first");

            RopeUtil.ConnectionType type;
            try {
                type = RopeUtil.ConnectionType.valueOf(tag.getString("type"));
            } catch (Exception e) {
                resetState(serverLevel, stack);
                return InteractionResultHolder.fail(stack);
            }

            BlockPos firstPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
            Long shipId = tag.getLong("shipId");
            String dim = tag.getString("dim");



            PhysPulleyBlockEntity pulley = null;
            if (type == RopeUtil.ConnectionType.PULLEY &&
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

        resetState(level, stack);
    }

    private void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message." + name),
                true
        );
    }


    private void resetState(ServerLevel level, ItemStack stack) {
        if (isFoil(stack)) {
            CompoundTag tag = stack.getTag().getCompound("first");

            if (RopeUtil.ConnectionType.valueOf(tag.getString("type")) == RopeUtil.ConnectionType.PULLEY) {
                PhysPulleyBlockEntity pulleyBE = (PhysPulleyBlockEntity) level.getBlockEntity(NbtUtils.readBlockPos(tag.getCompound("pos")));
                if (pulleyBE != null) pulleyBE. open();
            }

            stack.setTag(null);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("first");
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

wren (12/15/25)
 */