package yay.evy.everest.vstuff.content.constraint;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientRopeUtil;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyItem;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.RopeStyles;

public class LeadConstraintItem extends Item {
    private BlockPos firstClickedPos;
    private Long firstShipId;
    private Entity firstEntity;
    private Integer activeConstraintId;
    private ResourceKey<Level> firstClickDimension;

    public LeadConstraintItem(Properties pProperties) {
        super(pProperties);
    }



    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos().immutable();
        Player player = context.getPlayer();


        if (level.isClientSide()) {
            ClientRopeUtil.drawOutline(level, clickedPos);

            return InteractionResult.SUCCESS;
        }


        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.PASS;
        }
//a

//        PhysPulleyBlockEntity pulley = PhysPulleyItem.getWaitingPulley(player);
//        if (pulley != null) {
//            pulley.targetPos = clickedPos;
//            pulley.hasTarget = true;
//            pulley.waitingForTarget = false;
//            pulley.setChanged();
//            pulley.sendData();
//
//            pulley.createManualConstraint();
//
//            player.sendSystemMessage(Component.literal("§aPulley target set and constraint created!"));
//            PhysPulleyItem.clearWaitingPulley(player);
//
//            return InteractionResult.SUCCESS;
//        }


        if (firstClickedPos == null && firstEntity == null) {
            firstClickedPos = clickedPos;
            firstShipId = getShipIdAtPos(serverLevel, clickedPos);
            firstClickDimension = serverLevel.dimension();
            player.displayClientMessage(
                    Component.translatable("vstuff.message.rope_first"),
                    true
            );
            return InteractionResult.SUCCESS;

        } else {
            if (firstClickedPos != null && (player.isShiftKeyDown() || firstClickedPos.equals(clickedPos))) {
                resetState();
                player.displayClientMessage(
                        Component.translatable("vstuff.message.rope_reset"),
                        true
                );
                return InteractionResult.SUCCESS;
            }

            if (!serverLevel.dimension().equals(firstClickDimension)) {
                player.displayClientMessage(
                        Component.translatable("vstuff.message.interdimensional_fail"),
                        true
                );
                resetState();
                return InteractionResult.FAIL;
            }

            Long secondShipId = getShipIdAtPos(serverLevel, clickedPos);
            RopeUtil.RopeReturn ropeReturn = Rope.createNew(this, serverLevel, firstClickedPos, clickedPos, firstEntity, firstShipId, secondShipId, player);

            if (ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS) {

                Component notif = RopeStyleHandlerServer.getStyle(player.getUUID()).getBasicStyle() == RopeStyles.PrimitiveRopeStyle.CHAIN
                        ? Component.translatable("vstuff.message.chain_created")
                        : Component.translatable("vstuff.message.rope_created");

                player.displayClientMessage(notif, true);
            }

            resetState();
            return ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
    }

    public void setActiveConstraintId(Integer id) {
        this.activeConstraintId = id;
    }

    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        Ship shipObject = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        return shipObject != null ? shipObject.getId() : null;
    }

    private void resetState() {
        firstClickedPos = null;
        firstShipId = null;
        firstEntity = null;
        firstClickDimension = null;

        VStuff.LOGGER.info("Reset LeadConstraintItem state");
    }


}