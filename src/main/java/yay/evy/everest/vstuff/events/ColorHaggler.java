package yay.evy.everest.vstuff.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.constraint.ConstraintPersistence;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.content.constraint.RopeUtil;
import yay.evy.everest.vstuff.util.RopeStyles;


@Mod.EventBusSubscriber(modid = VStuff.MOD_ID)
public class ColorHaggler {

    @SubscribeEvent
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        try {
            ItemStack itemStack = event.getItemStack();
            Item item = event.getItemStack().getItem();
            Level level = event.getLevel();
            Player player = event.getEntity();


            if (level instanceof ServerLevel serverLevel) {
                Integer targetConstraintId = RopeUtil.findTargetedLead(serverLevel, player);
                if (player instanceof ServerPlayer serverPlayer) {
                    if (itemStack.is(Tags.Items.DYES)) {
                        if (!event.getLevel().isClientSide) {

                            if (ropeIsColorable(serverLevel, player)) {

                                String persistenceId = ConstraintTracker.persistanceIdViaConstraintId(targetConstraintId);
                                ConstraintTracker.RopeConstraintData data = ConstraintTracker.getActiveConstraints().get(targetConstraintId);
                                ConstraintTracker.RopeConstraintData newData = data;
                                ConstraintPersistence constraintPersistence = ConstraintPersistence.get(serverLevel);

                                newData.style = RopeStyles.fromString(item.toString());

                                ConstraintTracker.getActiveConstraints().put(targetConstraintId, newData);

                                NetworkHandler.sendConstraintRerender( targetConstraintId, newData.shipA, newData.shipB
                                        , newData.localPosA, newData.localPosB, newData.maxLength, newData.style);
                                constraintPersistence.addConstraint(persistenceId, newData.shipA, newData.shipB
                                        , newData.localPosA, newData.localPosB, newData.maxLength, newData.compliance, newData.maxForce, serverLevel, newData.constraintType, null, newData.style);
                                ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);


                                ConstraintPersistence.get(serverLevel).saveNow(serverLevel);

                                itemStack.shrink(1);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            VStuff.LOGGER.error("Error setting rope color: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean ropeIsColorable(ServerLevel serverLevel, Player player){
        return findRopesBasicStyle(serverLevel, player).equals("DYED") || findRopesBasicStyle(serverLevel, player).equals("WOOL");
    }

    private String findRopesBasicStyle(ServerLevel level, Player player) {
        Integer targetConstraintId = RopeUtil.findTargetedLead(level, player);

        ConstraintTracker.RopeConstraintData wizardsWorstKeptSecret = ConstraintTracker.getActiveConstraints().get(targetConstraintId);

        return wizardsWorstKeptSecret.style.getBasicStyle().toString();
    }

}