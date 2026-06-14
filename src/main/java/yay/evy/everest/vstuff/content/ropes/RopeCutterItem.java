package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.phys_ropes.PhysRope;
import yay.evy.everest.vstuff.content.ropes.phys_ropes.PhysRopeFactory;
import yay.evy.everest.vstuff.content.ropes.phys_ropes.PhysRopeManager;
import yay.evy.everest.vstuff.content.ropes.util.ILikeRopes;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

public class RopeCutterItem extends Item implements ILikeRopes {
    public RopeCutterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(itemStack);
        }

        PhysRope physRope = RopeUtils.findPhysRope(serverLevel, player);
        if (physRope != null) {
            try {
                PhysRopeFactory.destroyPhysRope(serverLevel, physRope);

                player.displayClientMessage(
                        VStuff.translate("message.rope.break"),
                        true
                );

                RopeUtils.playSound(serverLevel, physRope.posData0.blockPos(), physRope.style.breakSound());
                RopeUtils.playSound(serverLevel, physRope.posData1.blockPos(), physRope.style.breakSound());

                if (!player.isCreative()) {
                    itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                    createRopeDrop(player, physRope.style.id());
                }

                return InteractionResultHolder.success(itemStack);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

            if (!player.isCreative() && rope.hasDrop) {
                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                createRopeDrop(player, rope.style.id());
            }

            return InteractionResultHolder.success(itemStack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return InteractionResultHolder.pass(itemStack);
    }
}