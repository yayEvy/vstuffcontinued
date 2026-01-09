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

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.rope.roperework.NewRope;
import yay.evy.everest.vstuff.content.rope.roperework.NewRopeUtils;
import yay.evy.everest.vstuff.content.rope.roperework.RopeManager;
import yay.evy.everest.vstuff.content.rope.roperework.RopePersistence;
import yay.evy.everest.vstuff.foundation.RopeStyles;
import yay.evy.everest.vstuff.foundation.network.NetworkManager;


public class ColorHaggler {

    @SubscribeEvent
    public static void onPlayerColorItem(PlayerInteractEvent.RightClickItem event) {
        try {
            ItemStack itemStack = event.getItemStack();
            Item item = event.getItemStack().getItem();
            Level level = event.getLevel();
            Player player = event.getEntity();

            if (level instanceof ServerLevel serverLevel) {
                Integer targetConstraintId = NewRopeUtils.getTargetedRope(serverLevel, player);
                if (player instanceof ServerPlayer serverPlayer) {
                    if (itemStack.is(Tags.Items.DYES)) {
                        if (!event.getLevel().isClientSide) {

                            if (ropeIsColorable(serverLevel, player)) {

                                NewRope rope = RopeManager.getActiveRopes().get(targetConstraintId);
                                RopePersistence ropePersistence = RopePersistence.getOrCreate(serverLevel);

                                rope.style = getDyedStyle(item.toString(), getStyleOfTargetedRopeSoWeCanJudgeItAndDecideItsFate(serverLevel, player));

                                RopeManager.replaceRope(rope.ropeId, rope);

                                NetworkManager.sendRopeRerender(targetConstraintId, rope.posData0, rope.posData1, rope.jointValues.maxLength(), rope.style.getStyle());
                                ropePersistence.addRope(rope);
                                RopeManager.syncAllRopesToPlayer(serverPlayer);

                                RopePersistence.getOrCreate(serverLevel).saveNow(serverLevel);
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

    private static boolean ropeIsColorable(ServerLevel serverLevel, Player player){
        return getStyleOfTargetedRopeSoWeCanJudgeItAndDecideItsFate(serverLevel, player).toString().equals("DYE")
                || getStyleOfTargetedRopeSoWeCanJudgeItAndDecideItsFate(serverLevel, player).toString().equals("WOOL");
    }

    private static RopeStyles.PrimitiveRopeStyle getStyleOfTargetedRopeSoWeCanJudgeItAndDecideItsFate(ServerLevel level, Player player) {
        Integer targetConstraintId = NewRopeUtils.getTargetedRope(level, player);

        NewRope wizardsWorstKeptSecret = RopeManager.getActiveRopes().get(targetConstraintId);

        return wizardsWorstKeptSecret.style.getBasicStyle();
    }

    private static RopeStyles.RopeStyle getDyedStyle(String dyeItem, RopeStyles.PrimitiveRopeStyle type) {
        String color = dyeItem.split("_")[0]; // splits purple_dye into { purple, dye }, then we just get the first element, therefore only getting the desired color of the rope
        return RopeStyles.fromString(color + "_" + type.toString().toLowerCase());
    }

}
