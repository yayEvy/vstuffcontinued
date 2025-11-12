package yay.evy.everest.vstuff.content.physgrabber;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.client.PhysGrabberClientHandler;

public class PhysGrabberItem extends Item {
    public PhysGrabberItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            Minecraft mc = Minecraft.getInstance();
            PhysGrabberClientHandler.tryGrabOrRelease(mc, player);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
