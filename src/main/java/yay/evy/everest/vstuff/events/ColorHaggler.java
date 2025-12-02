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
import yay.evy.everest.vstuff.content.constraint.Rope;
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

                                Rope rope = ConstraintTracker.getActiveRopes().get(targetConstraintId);
                                ConstraintPersistence constraintPersistence = ConstraintPersistence.get(serverLevel);

                                rope.style = getDyedStyle(item.toString(), getStyleOfTargetedRopeSoWeCanJudgeItAndDecideItsFate(serverLevel, player));

                                ConstraintTracker.replaceConstraint(rope.ID, rope);

                                NetworkHandler.sendConstraintRerender(targetConstraintId, rope.shipA, rope.shipB,
                                        rope.localPosA, rope.localPosB, rope.maxLength, rope.style);
                                constraintPersistence.addConstraint(rope);
                                ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);

                                ConstraintPersistence.get(serverLevel).saveNow(serverLevel);
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
        return getStyleOfTargetedRopeSoWeCanJudgeItAndDecideItsFate(serverLevel, player).toString().equals("DYE")
                || getStyleOfTargetedRopeSoWeCanJudgeItAndDecideItsFate(serverLevel, player).toString().equals("WOOL");
    }

    private RopeStyles.PrimitiveRopeStyle getStyleOfTargetedRopeSoWeCanJudgeItAndDecideItsFate(ServerLevel level, Player player) {
        Integer targetConstraintId = RopeUtil.findTargetedLead(level, player);

        Rope wizardsWorstKeptSecret = ConstraintTracker.getActiveRopes().get(targetConstraintId);

        return wizardsWorstKeptSecret.style.getBasicStyle();
    }

    private RopeStyles.RopeStyle getDyedStyle(String dyeItem, RopeStyles.PrimitiveRopeStyle type) {
        String color = dyeItem.split("_")[0]; // splits purple_dye into { purple, dye }, then we just get the first element, therefore only getting the desired color of the rope
        return RopeStyles.fromString(color + "_" + type.toString().toLowerCase());
    }

}
