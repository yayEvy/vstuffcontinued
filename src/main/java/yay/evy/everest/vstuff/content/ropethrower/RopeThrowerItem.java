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

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
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









}