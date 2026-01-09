package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.util.packet.RequestCreativeRopeEditorPacket;
import yay.evy.everest.vstuff.content.ropes.RopeUtil;

public class CreativeRopeEditorItem extends Item {

    public CreativeRopeEditorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            Integer targetedRopeId = RopeUtil.findTargetedLead(
                    context.getLevel().getServer().getLevel(context.getLevel().dimension()),
                    context.getPlayer()
            );

            if (targetedRopeId != null) {
                VStuffPackets.sendToServer(new RequestCreativeRopeEditorPacket(targetedRopeId));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
