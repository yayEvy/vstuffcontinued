package yay.evy.everest.vstuff.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RopeRendererType extends RenderType {
    private RopeRendererType(String name, VertexFormat fmt, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, fmt, mode, bufSize, affectsCrumbling, sortOnUpload, setup, clear);
    }

    public static RenderType ropeRenderer(ResourceLocation texture) {
        return create(
                "rope_renderer",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                256,
                false,
                false,
                CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setOutputState(MAIN_TARGET)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false)
        );
    }
}
