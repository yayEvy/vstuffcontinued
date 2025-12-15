package yay.evy.everest.vstuff.content.constraint;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientOutlineHandler;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.pulley.*;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.Objects;

public class LeadConstraintItem extends Item {

    public LeadConstraintItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos().immutable();
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();


        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.PASS;
        }


        if (!isFoil(heldItem)) {
            ConnectionType connectionType = ConnectionType.NORMAL;
            if (serverLevel.getBlockEntity(clickedPos) instanceof PhysPulleyBlockEntity pulleyBE) {
                if (player.isShiftKeyDown()) { // nothing
                    return InteractionResult.FAIL;
                } else if (!pulleyBE.canAttachManualConstraint) {
                    resetStateWithMessage(serverLevel, heldItem, player, "pulley_attach_fail");
                    NetworkHandler.sendOutline(clickedPos, ClientOutlineHandler.RED);
                    return InteractionResult.FAIL;

                } else {
                    pulleyBE.setWaitingLeadConstraintItem(this);
                    connectionType = ConnectionType.PULLEY;
                }
            }

            if (connectionType == ConnectionType.NORMAL) {
                sendRopeMessage(player, "rope_first");
            } else {
                sendRopeMessage(player, "pulley_first");
            }

            Long tempId = getShipIdAtPos(serverLevel, clickedPos);
            Long actualId = tempId != null ? tempId : RopeUtil.getGroundBodyId(serverLevel);

            CompoundTag tag = heldItem.getOrCreateTagElement("first");
            tag.putBoolean("hasFirst", true);
            tag.put("pos", NbtUtils.writeBlockPos(clickedPos));
            tag.putLong("shipId", actualId);
            tag.putString("dim", serverLevel.dimension().location().toString());
            tag.putString("type", connectionType.name());
            NetworkHandler.sendOutline(clickedPos, ClientOutlineHandler.GREEN);
            return InteractionResult.SUCCESS;

        } else {
            CompoundTag tag = heldItem.getTag().getCompound("first");
            BlockPos firstClickedPos = NbtUtils.readBlockPos(tag.getCompound("pos"));
            Long firstShipId = tag.getLong("shipId");
            ConnectionType connectionType = ConnectionType.valueOf(tag.getString("type"));

            Long secondShipId = getShipIdAtPos(serverLevel, clickedPos);

            if (player.isShiftKeyDown() || firstClickedPos.equals(clickedPos)) {
                resetStateWithMessage(serverLevel, heldItem, player, "rope_reset");
                NetworkHandler.sendOutline(clickedPos, ClientOutlineHandler.GREEN);
                return InteractionResult.SUCCESS;
            }

            if (!serverLevel.dimension().location().toString().equals(tag.getString("dim"))) {
                resetStateWithMessage(serverLevel, heldItem, player, "interdimensional_fail");
                NetworkHandler.sendOutline(clickedPos, ClientOutlineHandler.RED);
                return InteractionResult.FAIL;
            }

            if (connectionType == ConnectionType.NORMAL) {

            } else {
                if (serverLevel.getBlockEntity(clickedPos) instanceof PulleyAnchorBlockEntity pulleyAnchorBE && connectionType == ConnectionType.PULLEY) {
                    if (Objects.equals(secondShipId, firstShipId)) { // pulley and anchor cannot be in same body
                        resetStateWithMessage(serverLevel, heldItem, player, "pulley_body_fail");
                        NetworkHandler.sendOutline(clickedPos, ClientOutlineHandler.RED);
                        return InteractionResult.FAIL;
                    }

                    RopeUtil.RopeReturn ropeReturn = Rope.createNew(this, serverLevel, firstClickedPos, clickedPos, firstShipId, secondShipId, player);
                    if (ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS) {
                        PhysPulleyBlockEntity waitingPulley = (PhysPulleyBlockEntity) serverLevel.getBlockEntity(firstClickedPos);
                        waitingPulley.attachRopeAndAnchor(ropeReturn.rope(), pulleyAnchorBE);
                    }
                } else {
                    resetStateWithMessage(serverLevel, heldItem, player, "pulley_fail");
                    NetworkHandler.sendOutline(clickedPos, ClientOutlineHandler.RED);
                    return InteractionResult.FAIL;
                }
            }

            RopeUtil.RopeReturn ropeReturn = Rope.createNew(this, serverLevel, firstClickedPos, clickedPos, firstShipId, secondShipId, player);

            if (ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS) {

                boolean isChain =
                        RopeStyleHandlerServer.getStyle(player.getUUID())
                                        .getBasicStyle() == RopeStyles.PrimitiveRopeStyle.CHAIN;

                serverLevel.playSound(
                        null,
                        clickedPos,
                        isChain
                                ? net.minecraft.sounds.SoundEvents.CHAIN_PLACE
                                : net.minecraft.sounds.SoundEvents.LEASH_KNOT_PLACE,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                NetworkHandler.sendOutline(clickedPos, ClientOutlineHandler.GREEN);
                resetStateWithMessage(serverLevel, heldItem, player, isChain ? "chain_created" : "rope_created");

            }

            return ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
    }

    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.hasTag() && pStack.getTag().contains("first");
    }

    private void resetState(ServerLevel level, ItemStack stack) {
        if (isFoil(stack)) {
            CompoundTag tag = stack.getTag().getCompound("first");

            if (ConnectionType.valueOf(tag.getString("type")) == ConnectionType.PULLEY) {
                PhysPulleyBlockEntity pulleyBE = (PhysPulleyBlockEntity) level.getBlockEntity(NbtUtils.readBlockPos(tag.getCompound("pos")));

                if (pulleyBE != null) {
                    pulleyBE.clearWaitingLeadConstraintItem();
                    pulleyBE.resetSelf();
                }
            }

            stack.setTag(null);
        }

        VStuff.LOGGER.info("Successfully reset LeadConstraintItem");
    }

    private void resetStateWithMessage(ServerLevel level, ItemStack stack, Player player, String name) {
        sendRopeMessage(player, name);

        resetState(level, stack);
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