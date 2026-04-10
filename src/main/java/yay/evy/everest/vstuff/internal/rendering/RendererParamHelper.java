package yay.evy.everest.vstuff.internal.rendering;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.util.Locale;

import static yay.evy.everest.vstuff.VStuff.asResource;
import static yay.evy.everest.vstuff.VStuff.mcResource;

public class RendererParamHelper {

    public static JsonObject textureParams(ResourceLocation texture, float scale) {
        JsonObject p = new JsonObject();
        p.addProperty("texture", texture.toString());
        p.addProperty("scale", scale);
        return p;
    }

    private JsonObject halfSizeTextureParams(ResourceLocation texture) {
        return textureParams(texture, 0.5f);
    }

    public static JsonObject textureParams(ResourceLocation texture) {
        return textureParams(texture, 1.0f);
    }

    public static JsonObject block(String name) {
        return textureParams(mcResource("textures/block/" + name + ".png"));
    }

    public static JsonObject item(String name) {
        return textureParams(mcResource("textures/item/" + name + ".png"));
    }

    public static JsonObject modResource(String name) {
        return textureParams(asResource("textures/rope/rope_" + name + ".png"));
    }

    public static JsonObject colorParams(String hex) {
        JsonObject p = new JsonObject();
        p.addProperty("color", hex);
        return p;
    }

    public static JsonObject defaultTexture() {
        return textureParams(VStuff.asResource("textures/rope/rope_normal.png"));
    }

    public static String sanitizeFileName(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
    }

}
