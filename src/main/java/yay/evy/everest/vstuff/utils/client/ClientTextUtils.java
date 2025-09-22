package yay.evy.everest.vstuff.utils.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

// From Steam 'n Rails
public class ClientTextUtils {
    /**
     * Pass in a component, and a width in pixels,
     * and it'll return a component that if it overflows after the cutoff,
     * it will remove the extra text and add `...` at the end
     */
    @OnlyIn(Dist.CLIENT)
    public static Component getComponentWithWidthCutoff(Component component, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        if (font.width(component) > maxWidth) {
            String substr = font.plainSubstrByWidth(component.getString(), maxWidth);
            if (substr.endsWith(" ")) {
                substr = substr.substring(0, substr.length() - 1) + "...";
            } else {
                substr += "...";
            }
            return Component.literal(substr);
        }
        return component;
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderMultilineDebugText(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                                double baseY, boolean transparent, String... lines) {
        double y = baseY + (lines.length/4.0D);
        for (String line : lines) {
            renderDebugText(poseStack, buffer, packedLight, y, transparent, line);
            y -= 0.25D;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderDebugText(PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight,
                                       double y, boolean transparent, String text) {
        poseStack.pushPose();
        poseStack.translate(0.0D, y, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getBlockEntityRenderDispatcher().camera.rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = poseStack.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;
        Font font = Minecraft.getInstance().font;
        float f2 = (float)(-font.width(text) / 2);
        font.drawInBatch(text, f2, 0, 553648127, false, matrix4f, pBuffer, transparent ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, pPackedLight);

        if (transparent) {
            font.drawInBatch(text, f2, 0, -1, false, matrix4f, pBuffer, Font.DisplayMode.NORMAL, 0, pPackedLight);
        }

        poseStack.popPose();
    }
}