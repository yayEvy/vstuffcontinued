package yay.evy.everest.vstuff.content.ropes.editor;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

public class RopeEditorItem extends Item {
    public RopeEditorItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResultHolder.pass(stack);

        Integer ropeId =  RopeUtils.findTargetedLead(serverLevel, player);

        if (ropeId == null || RopeManager.getRope(ropeId) == null) {
            player.displayClientMessage(VStuff.translate("rope.editor_not_found").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.success(stack);
        }

        ScreenOpener.open(new RopeEditorScreen(RopeManager.getRope(ropeId)));

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
}
