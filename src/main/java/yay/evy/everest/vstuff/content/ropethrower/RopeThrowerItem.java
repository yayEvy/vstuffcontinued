package yay.evy.everest.vstuff.content.ropethrower;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.client.ClientOutlineHandler;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.constraint.RopeUtil;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.index.VStuffSounds;

public class RopeThrowerItem extends Item {

    private BlockPos firstClickedPos;
    private Long firstShipId;
    private ResourceKey<Level> firstClickDimension;
    private boolean hasFirst = false;
    private RopeUtil.ConnectionType connectionType;
    private PhysPulleyBlockEntity waitingPulley;

    public RopeThrowerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            BlockHitResult hit = Item.getPlayerPOVHitResult(
                    level,
                    player,
                    net.minecraft.world.level.ClipContext.Fluid.NONE
            );

            if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                NetworkHandler.sendOutline(hit.getBlockPos(), ClientOutlineHandler.GREEN);
            }

            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }


        if (!hasFirst) {

            BlockHitResult hit = Item.getPlayerPOVHitResult(
                    level,
                    player,
                    net.minecraft.world.level.ClipContext.Fluid.NONE
            );

            if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
                player.displayClientMessage(
                        Component.translatable("vstuff.message.rope_thrower_no_block"),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            BlockPos clickedPos = hit.getBlockPos().immutable();

            firstClickedPos = clickedPos;
            firstShipId = getShipIdAtPos(serverLevel, clickedPos);
            firstClickDimension = serverLevel.dimension();

            if (serverLevel.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulley) {
                if (!pulley.canAttach()) {
                    player.displayClientMessage(
                            Component.translatable("vstuff.message.pulley_attach_fail"),
                            true
                    );
                    resetState();
                    return InteractionResultHolder.fail(stack);
                }

                pulley.setWaiting();
                waitingPulley = pulley;
                connectionType = RopeUtil.ConnectionType.PULLEY;

                player.displayClientMessage(
                        Component.translatable("vstuff.message.pulley_first"),
                        true
                );
            } else {
                connectionType = RopeUtil.ConnectionType.NORMAL;
                player.displayClientMessage(
                        Component.translatable("vstuff.message.rope_first"),
                        true
                );
            }

            hasFirst = true;
            return InteractionResultHolder.success(stack);
        }

        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                VStuffSounds.ROPE_THROW.get(),
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
        );

        RopeThrowerEntity entity = new RopeThrowerEntity(level, player);
        entity.setItem(stack);
        entity.setStartData(
                firstClickedPos,
                firstShipId,
                firstClickDimension,
                connectionType,
                waitingPulley
        );

        entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(entity);

        resetState();

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }

    private void resetState() {
        firstClickedPos = null;
        firstShipId = null;
        firstClickDimension = null;
        hasFirst = false;
    }

    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip ship = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return ship != null ? ship.getId() : null;
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







