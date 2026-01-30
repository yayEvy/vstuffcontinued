package yay.evy.everest.vstuff.content.rope.ropethrower;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientOutlineHandler;
import yay.evy.everest.vstuff.content.rope.roperework.RopeUtil;
import yay.evy.everest.vstuff.content.rope.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.foundation.network.NetworkManager;
import yay.evy.everest.vstuff.foundation.utility.PosUtils;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffSounds;

public class RopeThrowerItem extends Item {

    public RopeThrowerItem(Properties properties) {
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

        String blockName = serverLevel.getBlockState(clickedPos).getBlock().getName().getString();

        if (PosUtils.isPulleyAnchor(state)) {
            if (!level.isClientSide) firstFailWithMessage(player, clickedPos, "invalid_first", blockName);
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulley && !pulley.canAttach()) {
            if (!level.isClientSide) firstFailWithMessage(player, clickedPos, "invalid_second", blockName);
            return InteractionResult.SUCCESS;
        }
        firstSelect(serverLevel, clickedPos, player, heldItem);
        return InteractionResult.SUCCESS;
    }

    private void firstSelect(ServerLevel level, BlockPos clickedPos, Player player, ItemStack heldItem) {
        RopeUtil.SelectType selection = RopeUtil.SelectType.NORMAL;
        if (level.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulleyBE && pulleyBE.canAttach()) {
            pulleyBE.setWaiting();
            selection = RopeUtil.SelectType.PULLEY;
        }

        if (selection == RopeUtil.SelectType.NORMAL) {
            player.displayClientMessage(VStuff.translate("rope.first").withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(VStuff.translate("rope.pulley_first").withStyle(ChatFormatting.GREEN), true);
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

    private void firstFailWithMessage(Player player, BlockPos clickedPos, String message, Object... args) {
        player.displayClientMessage(VStuff.translate("rope." + message, args).withStyle(ChatFormatting.RED), true);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkManager.sendOutlineToPlayer(serverPlayer, clickedPos, ClientOutlineHandler.RED);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!isFoil(stack)) {
            player.displayClientMessage(VStuff.translate("rope.thrower_not_set").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        if (isFoil(stack)) {
            ItemStack thrown = stack.copy();
            thrown.setCount(1);

            RopeThrowerEntity entity = new RopeThrowerEntity(VStuffEntities.ROPE_THROWER.get(), serverLevel);
            entity.setOwner(player);
            entity.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

            CompoundTag tag = stack.getTag().getCompound("first");

            RopeUtil.SelectType type = RopeUtil.SelectType.valueOf(tag.getString("type"));
            BlockPos startPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
            Long shipId = tag.getLong("shipId");
            String dim = tag.getString("dim");


            entity.setStartData(
                    startPos,
                    shipId,
                    dim,
                    type
            );

            entity.setItem(thrown);
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1.0F);

            serverLevel.addFreshEntity(entity);

            stack.setTag(null);

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            serverLevel.playSound(
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