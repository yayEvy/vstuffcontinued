package yay.evy.everest.vstuff.content.handle;

import com.simibubi.create.foundation.item.CustomArmPoseItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HandleItem extends Item implements CustomArmPoseItem {

    public HandleItem(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
        if (HandleClientHandler.isHolding(player)) {
            return ArmPose.THROW_SPEAR;
        }
        return ArmPose.ITEM;
    }
}
