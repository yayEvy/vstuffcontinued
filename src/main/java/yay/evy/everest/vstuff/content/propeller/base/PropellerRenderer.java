package yay.evy.everest.vstuff.content.propeller.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.content.propeller.WoodenPropellerBlock;

public class PropellerRenderer<T extends AbstractPropellerBlockEntity> extends KineticBlockEntityRenderer<T> {

    public PropellerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        if (!be.getBlockState().getValue(AbstractPropellerBlock.HAS_BLADES)) return;

        float speed = be.visualSpeed.getValue(partialTicks) * 3 / 10f;
        float angle = be.angle + speed * partialTicks;

        VertexConsumer vb = buffer.getBuffer(RenderType.cutout());
        renderPropeller(be, ms, light, angle, vb);
    }

    private void renderPropeller(T be, PoseStack ms, int light, float angle, VertexConsumer vb) {
        PartialModel model = be.getPartialBladeModel();
        if (model == null) return;

        SuperByteBuffer prop = CachedBuffers.partial(model, be.getBlockState());

        kineticRotationTransform(prop, be, getRotationAxisOf(be), AngleHelper.rad(angle), light);

        prop.renderInto(ms, vb);
    }

    @Override
    protected BlockState getRenderedBlockState(T be) {
        return shaft(getRotationAxisOf(be));
    }
}