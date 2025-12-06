package yay.evy.everest.vstuff.content.handle;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HandleRenderHandler {

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;
        if (!HandleClientHandler.isHolding(player)) return;

        Vec3 handlePos = HandleClientHandler.getHandlePos(player);
        if (handlePos == null) return;

        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        Vec3 shoulderPos = player.getEyePosition().subtract(0, 0.2, 0);

        Vec3 dir = handlePos.subtract(shoulderPos).normalize();

        float xRot = (float) -Math.asin(dir.y);

        float worldYaw = (float) Math.atan2(dir.z, dir.x);
        float playerYaw = (float) Math.toRadians(player.yBodyRot);

        float yRot = worldYaw - playerYaw;


        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        model.leftArmPose  = HumanoidModel.ArmPose.EMPTY;

        model.rightArm.xRot = xRot;
        model.rightArm.yRot = yRot;

        model.leftArm.xRot  = xRot;
        model.leftArm.yRot  = yRot;
    }
}