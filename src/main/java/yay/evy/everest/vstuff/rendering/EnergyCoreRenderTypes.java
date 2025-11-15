package yay.evy.everest.vstuff.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

import java.util.function.Function;

public class EnergyCoreRenderTypes extends RenderType {

    public EnergyCoreRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    public static Function<ResourceLocation, RenderType> getCORE = Util.memoize(EnergyCoreRenderTypes::CORE);

    public static RenderType CORE(ResourceLocation resourceLocation) {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_GLINT_SHADER)
                .setTextureState(new TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true);

        return create("core_renderer",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                false,
                compositeState);
    }
}
