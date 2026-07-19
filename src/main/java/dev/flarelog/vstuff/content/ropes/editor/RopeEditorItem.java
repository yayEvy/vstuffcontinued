package dev.flarelog.vstuff.content.ropes.editor;

import dev.flarelog.vstuff.content.ropes.Rope;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.util.RopeUtil;

public class RopeEditorItem extends Item {
    public RopeEditorItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResultHolder.pass(stack);

        Rope rope = RopeUtil.findPhysRope(serverLevel, player);

        if (rope == null) {
            player.displayClientMessage(VStuff.translate("rope.editor_not_found").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.success(stack); // unimplemented feature of doom and despair
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
}
