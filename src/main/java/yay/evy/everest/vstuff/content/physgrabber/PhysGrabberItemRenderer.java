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
import yay.evy.everest.vstuff.rendering.EnergyCoreRenderTypes;

public class PhysGrabberItemRenderer extends CustomRenderedItemModelRenderer {


    private static final ResourceLocation EMPTY = VStuff.asTextureResource("block/empty.png");
    private static final ResourceLocation TINT = VStuff.asTextureResource("block/blue_tint.png");

    private static final PartialModel CORE = PartialModel.of(VStuff.asResource("item/phys_grabber/core"));
    private static final PartialModel CORE_INNER = PartialModel.of(VStuff.asResource("item/phys_grabber/core_inner"));
    private static final PartialModel CORE_OUTER = PartialModel.of(VStuff.asResource("item/phys_grabber/core_outer"));

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
        renderAndTransform(ms, CORE_INNER, RenderType.endPortal(), renderer, modelOffset, innerData.offset(), innerData.rotation(), 1.5f, light);
        renderAndTransform(ms, CORE, EnergyCoreRenderTypes.getCORE.apply(EMPTY), renderer, modelOffset, data.offset(), data.rotation(), 1.5f, light);
        renderAndTransform(ms, CORE_OUTER, RenderType.entityTranslucent(TINT), renderer, modelOffset, data.offset(), data.rotation(), 1.5f, light);
    }

    private void renderAndTransform(@NotNull PoseStack ms, @NotNull PartialModel model, @NotNull RenderType renderType, @NotNull PartialItemModelRenderer renderer, @NotNull Vector3f modelCorrection, @NotNull Vector3f offset, @NotNull Vector3f rotationVec, float scale, int light) {
        ms.pushPose();

        ms.scale(scale, scale, scale);

        ms.mulPose(Axis.YP.rotationDegrees(rotationVec.y()));
        ms.mulPose(Axis.XP.rotationDegrees(rotationVec.x()));
        ms.mulPose(Axis.ZP.rotationDegrees(rotationVec.z()));

        ms.translate(modelCorrection.x(), modelCorrection.y(), modelCorrection.z());

        renderer.render(model.get(), renderType, light);

        ms.popPose();
    }
}