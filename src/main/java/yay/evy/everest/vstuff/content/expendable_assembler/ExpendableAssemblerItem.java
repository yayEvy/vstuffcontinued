package yay.evy.everest.vstuff.content.expendable_assembler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;

import java.util.Set;

public class ExpendableAssemblerItem extends Item {

    public ExpendableAssemblerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (!level.isClientSide) {
            try {
                if (level instanceof ServerLevel serverLevel) {
                    Set<BlockPos> blocksToAssemble = Set.of(clickedPos);
                    ShipAssembler.assembleToShipFull(serverLevel, blocksToAssemble, 1);
                }

                stack.hurtAndBreak(1, context.getPlayer(), p -> p.broadcastBreakEvent(context.getHand()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
