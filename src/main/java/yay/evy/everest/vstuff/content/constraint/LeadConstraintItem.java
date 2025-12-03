package yay.evy.everest.vstuff.content.constraint;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientRopeUtil;
import yay.evy.everest.vstuff.content.pulley.*;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.Objects;

public class LeadConstraintItem extends Item {
    private BlockPos firstClickedPos;
    private Long firstShipId;
    private ConnectionType connectionType;
    public PhysPulleyBlockEntity waitingPulley;
    public boolean canAttachPhysPulley = true;
    private boolean hasFirst = false;
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


        if (!hasFirst) {
            firstClickedPos = clickedPos;
            firstShipId = getShipIdAtPos(serverLevel, clickedPos);
            firstClickDimension = serverLevel.dimension();

            if (serverLevel.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulleyBE) {
                if (player.isShiftKeyDown()) { // insert rope
                    resetStateWithMessage(player, "pulley_insert");
                    return pulleyBE.insertRope(player, context.getHand());

                } else if (!pulleyBE.canAttachManualConstraint) {
                    resetStateWithMessage(player, "pulley_attach_fail");
                    return InteractionResult.FAIL;

                } else {
                    pulleyBE.setWaitingLeadConstraintItem(this);
                    connectionType = ConnectionType.PULLEY;
                }
            } else {
                connectionType = ConnectionType.NORMAL;
            }

            if (connectionType == ConnectionType.NORMAL) {
                sendRopeMessage(player, "rope_first");
            } else {
                sendRopeMessage(player, "pulley_first");
            }
            hasFirst = true;
            return InteractionResult.SUCCESS;

        } else {
            if (player.isShiftKeyDown() || firstClickedPos.equals(clickedPos)) {
                resetStateWithMessage(player, "rope_reset");
                return InteractionResult.SUCCESS;
            }

            if (!serverLevel.dimension().equals(firstClickDimension)) {
                resetStateWithMessage(player, "interdimensional_fail");
                return InteractionResult.FAIL;
            }

            if (connectionType == ConnectionType.NORMAL) {

            } else {
                if (serverLevel.getBlockEntity(clickedPos) instanceof PulleyAnchorBlockEntity pulleyAnchorBE) {
                    Long secondShipId = getShipIdAtPos(serverLevel, clickedPos);
                    if (Objects.equals(secondShipId, firstShipId)) { // pulley and anchor cannot be in same body
                        resetStateWithMessage(player, "pulley_body_fail");
                        return InteractionResult.FAIL;
                    }

                    RopeUtil.RopeReturn ropeReturn = Rope.createNew(this, serverLevel, firstClickedPos, clickedPos, firstShipId, secondShipId, player);
                    System.out.println(ropeReturn.result());
                    System.out.println(ropeReturn.rope());
                    if (ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS) {
                        waitingPulley.attachRopeAndAnchor(ropeReturn.rope(), pulleyAnchorBE);
                    }
                } else {
                    resetStateWithMessage(player, "pulley_fail");
                    return InteractionResult.FAIL;
                }
            }

            Long secondShipId = getShipIdAtPos(serverLevel, clickedPos);
            RopeUtil.RopeReturn ropeReturn = Rope.createNew(this, serverLevel, firstClickedPos, clickedPos, firstShipId, secondShipId, player);

            if (ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS) {

                if (RopeStyleHandlerServer.getStyle(player.getUUID()).getBasicStyle() == RopeStyles.PrimitiveRopeStyle.CHAIN) {
                    resetStateWithMessage(player, "chain_created");
                } else {
                    resetStateWithMessage(player, "rope_created");
                }
            }

            return ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
    }

    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    private void resetState() {
        firstClickedPos = null;
        firstShipId = null;
        firstClickDimension = null;
        connectionType = null;
        hasFirst = false;
        if (waitingPulley != null) {
            waitingPulley.clearWaitingLeadConstraintItem();
        }

        VStuff.LOGGER.info("Successfully reset LeadConstraintItem");
    }

    private void resetStateWithMessage(Player player, String name) {
        sendRopeMessage(player, name);

        resetState();
    }

    enum ConnectionType {
        NORMAL,
        PULLEY
    }

    private void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message." + name),
                true
        );
    }

}