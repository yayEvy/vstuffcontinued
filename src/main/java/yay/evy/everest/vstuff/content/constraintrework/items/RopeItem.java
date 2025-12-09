package yay.evy.everest.vstuff.content.constraintrework.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;

public class RopeItem extends Item {
    private BlockPos firstClickedPos;
    private Long firstShipId;
    public PhysPulleyBlockEntity waitingPulley;
    public boolean canAttachPhysPulley = true;
    private boolean hasFirst = false;
    private ResourceKey<Level> firstClickDimension;

    public RopeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack heldStack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isShiftKeyDown() && hasFirst) { // allow to reset without looking at a block
            resetWithMessage(pPlayer, "rope_reset");
            return InteractionResultHolder.success(heldStack);
        } else {
            return super.use(pLevel, pPlayer, pUsedHand);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        return super.useOn(pContext);
    }

    private void reset() {
        firstClickedPos = null;
        firstShipId = null;
        firstClickDimension = null;
        hasFirst = false;
        if (waitingPulley != null) {
            waitingPulley.clearWaitingLeadConstraintItem();
        }
    }

    private void resetWithMessage(Player player, String name) {
        sendRopeMessage(player, name);

        reset();
    }

    private void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message." + name),
                true
        );
    }
}
