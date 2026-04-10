package yay.evy.everest.vstuff.content.ropes.arrow;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;

public class RopeArrowRenderer extends ArrowRenderer<RopeArrowEntity> {

    public RopeArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RopeArrowEntity entity) {
        return VStuff.asResource("textures/item/arrow.png");
    }
}