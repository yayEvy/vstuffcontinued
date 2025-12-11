package yay.evy.everest.vstuff.content.ropethrower;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.index.VStuffSounds;

import static net.minecraft.world.level.block.entity.BeaconBlockEntity.playSound;

public class RopeThrowerItem extends Item {
    public RopeThrowerItem(Properties pProperties) {
        super(pProperties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) { //Use (it uses)
        ItemStack itemStack = player.getItemInHand(hand); //gets item
        level.playSound((Player) null, player.getX(), player.getY(), player.getZ(),
                VStuffSounds.ROPE_THROW.get(), SoundSource.NEUTRAL, 1F, 1F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            RopeThrowerEntity ropeThrower = new RopeThrowerEntity(level,player);
            ropeThrower.setItem(itemStack);
            ropeThrower.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(ropeThrower);

        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
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







}