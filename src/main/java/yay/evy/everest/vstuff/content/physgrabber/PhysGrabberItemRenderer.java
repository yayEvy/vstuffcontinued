package yay.evy.everest.vstuff.content.physgrabber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.TransformData;
import yay.evy.everest.vstuff.index.VStuffPartialModels;
import yay.evy.everest.vstuff.rendering.EnergyCoreRenderTypes;

public class PhysGrabberItemRenderer extends CustomRenderedItemModelRenderer {


    private static final ResourceLocation CORE = VStuff.asTextureResource("block/empty.png");
    private static final ResourceLocation PURPLE_HUE = VStuff.asTextureResource("block/purple_hue.png");

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType,
                          PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderer.render(model.getOriginalModel(), light);

        TransformData data = new TransformData(new Vector3f(0.0F, -0.65F, 0.0F), new Vector3f(35.0F, -25.0F, 0.0F));
        TransformData innerData = new TransformData(new Vector3f(0.0F, -0.65F, 0.0F), new Vector3f(35.0F, -25.0F, 0.0F));

        renderCore(ms, renderer, innerData, data, light);
    }

    private void renderCore(@NotNull PoseStack ms, @NotNull PartialItemModelRenderer renderer, @NotNull TransformData innerData, @NotNull TransformData data, int light) {
        Vector3f modelOffset = new Vector3f(0.0F, -0.28125F, 0.0F);
        renderAndTransform(ms, VStuffPartialModels.GRABBER_CORE_INNER, RenderType.endPortal(), renderer, modelOffset, innerData.offset(), innerData.rotation(), 2.5f, light);
        renderAndTransform(ms, VStuffPartialModels.GRABBER_CORE, EnergyCoreRenderTypes.getCORE.apply(CORE), renderer, modelOffset, data.offset(), data.rotation(), 2.5f, light);
        renderAndTransform(ms, VStuffPartialModels.GRABBER_CORE_OUTER, RenderType.entityTranslucent(PURPLE_HUE), renderer, modelOffset, data.offset(), data.rotation(), 2.5f, light);
    }

    private void renderAndTransform(@NotNull PoseStack ms, @NotNull PartialModel model, @NotNull RenderType renderType, @NotNull PartialItemModelRenderer renderer, @NotNull Vector3f modelCorrection, @NotNull Vector3f offset, @NotNull Vector3f rotationVec, float scale, int light) {
        ms.pushPose();

        ms.translate(offset.x(), offset.y(), offset.z());
        ms.translate(0.25D, 0.25D, 0.25D);
        ms.pushPose();

        ms.scale(scale, scale, scale);

        ms.translate(-(1 / scale * 4), -(1 / scale * 4), -(1 / scale * 4));
        ms.translate(-(modelCorrection.x()), -(modelCorrection.y()), -(modelCorrection.z()));

        ms.mulPose(Axis.YP.rotationDegrees(rotationVec.y()));
        ms.mulPose(Axis.XP.rotationDegrees(rotationVec.x()));
        ms.mulPose(Axis.ZP.rotationDegrees(rotationVec.z()));

        ms.translate(modelCorrection.x(), modelCorrection.y(), modelCorrection.z());

        renderer.render(model.get(), renderType, light);

        ms.popPose();
        ms.popPose();
    }
}