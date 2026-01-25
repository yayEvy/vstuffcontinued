package yay.evy.everest.vstuff.content.expendable_assembler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;
import yay.evy.everest.vstuff.index.VStuffSounds;

import java.util.Set;

public class ExpendableAssemblerItem extends Item {

    private static final int COOLDOWN_TICKS = 20 * 5;

    public ExpendableAssemblerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (!(level instanceof ServerLevel serverLevel) || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        if (context.getPlayer().getCooldowns().isOnCooldown(this)) {
            return InteractionResult.CONSUME;
        }

        try {
            Set<BlockPos> blocksToAssemble = Set.of(clickedPos);
            ShipAssembler.assembleToShipFull(serverLevel, blocksToAssemble, 1);

            context.getPlayer().getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            stack.shrink(1);

            serverLevel.playSound(
                    null,
                    clickedPos,
                    VStuffSounds.ASSEMBLE.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.8F,
                    1.0F
            );

            return InteractionResult.sidedSuccess(level.isClientSide);


        } catch (Exception e) {
            e.printStackTrace();
            return InteractionResult.CONSUME;
        }
    }
}