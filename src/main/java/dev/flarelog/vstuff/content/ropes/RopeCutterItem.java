package dev.flarelog.vstuff.content.ropes;

import dev.flarelog.vstuff.content.ropes.type.RopeType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.util.ILikeRopes;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.util.RopeUtil;

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

        Rope physRope = RopeUtil.findPhysRope(serverLevel, player);
        if (physRope != null) {
            try {
                // doom and despair idfk

                player.displayClientMessage(
                        VStuff.translate("message.rope.break"),
                        true
                );

                RopeStyle style = physRope.getStyle(serverLevel.registryAccess());

                RopeUtil.playSound(serverLevel, physRope.posData0.blockPos(), style.breakSound());
                RopeUtil.playSound(serverLevel, physRope.posData1.blockPos(), style.breakSound());

                if (!player.isCreative()) {
                    itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                    createRopeDrop(player, physRope.styleKey);
                }

                return InteractionResultHolder.success(itemStack);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return InteractionResultHolder.pass(itemStack);
        }


        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    public ResourceKey<RopeType> getType() {
        return null;
    }
}