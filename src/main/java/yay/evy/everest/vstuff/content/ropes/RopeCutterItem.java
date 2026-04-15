package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.RopeRendererTypes;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public class RopeCutterItem extends Item implements ILikeRopes{
    public RopeCutterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(itemStack);
        }

        ReworkedRope rope = RopeUtils.findRope(serverLevel, player);
        if (rope == null) return InteractionResultHolder.pass(itemStack);

        try {
            RopeFactory.removeRope(serverLevel, rope.ropeId);

            player.displayClientMessage(
                    VStuff.translate("message.rope.break"),
                    true
            );

            RopeUtils.playSound(serverLevel, rope.posData0.blockPos(), rope.style.breakSound());
            RopeUtils.playSound(serverLevel, rope.posData1.blockPos(), rope.style.breakSound());

            if (!player.isCreative()) {
                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                ItemStack ropeStack = new ItemStack(VStuffItems.ROPE.get());
                addStyleToTag(ropeStack, rope.style);

                player.drop(ropeStack, false);
            }

            return InteractionResultHolder.success(itemStack);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return InteractionResultHolder.pass(itemStack);
    }

}