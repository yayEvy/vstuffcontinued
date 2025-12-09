package yay.evy.everest.vstuff.content.physgrabber;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.CustomArmPoseItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.Map;
import java.util.function.Consumer;

public class PhysGrabberItem extends Item implements CustomArmPoseItem {

    public PhysGrabberItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            Minecraft mc = Minecraft.getInstance();
            boolean didGrab = PhysGrabberClientHandler.tryGrabOrRelease(mc, player);
            if (didGrab) {
                return InteractionResultHolder.success(itemStack);
            }
        }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new PhysGrabberItemRenderer()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
        if (PhysGrabberClientHandler.isGrabbing(player)) {
            return ArmPose.CROSSBOW_HOLD;
        }
        return ArmPose.ITEM;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        Map<Enchantment, Integer> enchants = book.getAllEnchantments();
        for (Enchantment enchant : enchants.keySet()) {
            if (enchant != Enchantments.UNBREAKING || enchant != Enchantments.MENDING) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        return pRepairCandidate.getItem() == AllItems.BRASS_SHEET.get();
    }
}
