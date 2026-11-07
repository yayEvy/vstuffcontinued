package dev.flarelog.vstuff.content.ropes;

import dev.flarelog.vstuff.internal.utility.PositionUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.internal.utility.RopeUtil;

public class RopeCutterItem extends Item {
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
        if (physRope == null) {
            return InteractionResultHolder.pass(itemStack);
        }

        try {
            RopeStyle style = physRope.getStyle(serverLevel.registryAccess());
            var pos0 = PositionUtils.containingBlockPos(physRope.getPosData0().getWorldPos(serverLevel));
            var pos1 = PositionUtils.containingBlockPos(physRope.getPosData1().getWorldPos(serverLevel));
            var styleKey = physRope.getStyleKey();
            Integer ropeId = physRope.getRopeId();

            physRope.cleanup(serverLevel);

            RopeManager.get(serverLevel).removeRope(ropeId);

            player.displayClientMessage(VStuff.translate("message.rope.break"), true);
            RopeUtil.playSound(serverLevel, pos0, style.breakSound());
            RopeUtil.playSound(serverLevel, pos1, style.breakSound());

            if (!player.isCreative()) {
                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                RopeUtil.createRopeDrop(player, styleKey);
            }

            return InteractionResultHolder.success(itemStack);
        } catch (Exception e) {
            e.printStackTrace();
            return InteractionResultHolder.pass(itemStack);
        }
    }
}