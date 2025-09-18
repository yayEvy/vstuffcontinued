package yay.evy.everest.vstuff.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.world.entity.Display;

public class RopeRendererType extends RenderType {

    public RopeRendererType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                            boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }


    public static RenderType ropeRenderer(ResourceLocation texture) {
        return create("rope_renderer",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(NO_OVERLAY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(true));
    }


    public static RenderType ropeRendererWithTransparency(ResourceLocation texture) {
        return create("rope_renderer_translucent",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(NO_OVERLAY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(true));
    }
}
