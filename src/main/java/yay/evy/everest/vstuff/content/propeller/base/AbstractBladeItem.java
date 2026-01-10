package yay.evy.everest.vstuff.content.propeller.base;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBladeItem extends Item {

    public AbstractBladeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        Player player = pContext.getPlayer();

        if (!(level instanceof ServerLevel) || player == null) {
            return InteractionResult.PASS;
        }

        if (level.getBlockEntity(clickedPos) instanceof AbstractPropellerBlockEntity propellerBlockEntity) {
            BlockState engineState = level.getBlockState(clickedPos);
            if (!engineState.getValue(AbstractPropellerBlock.HAS_BLADES)) {
                propellerBlockEntity.setPartialBladeModel(getBladeModel());
                propellerBlockEntity.setPower(getPower());
                propellerBlockEntity.setWaterPower(getWaterPower());
                propellerBlockEntity.setStressImpact(getStressImpact());
                propellerBlockEntity.setMaxThrust(getMaxThrust()); // i hate this
                System.out.println(getBladeModel());
                System.out.println(propellerBlockEntity.getPartialBladeModel());
                level.setBlock(clickedPos, engineState.setValue(AbstractPropellerBlock.HAS_BLADES, true), 3);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(
                        Component.translatable("vstuff.message.has_blades"),
                        true
                );
                return InteractionResult.FAIL;
            }
        }

        player.displayClientMessage(
                Component.translatable("vstuff.message.propeller_fail"),
                true
        );

        return InteractionResult.FAIL;
    }

    public abstract PartialModel getBladeModel();
    public abstract double getPower();
    public abstract double getWaterPower();
    public abstract double getStressImpact();
    public abstract double getMaxThrust();
}