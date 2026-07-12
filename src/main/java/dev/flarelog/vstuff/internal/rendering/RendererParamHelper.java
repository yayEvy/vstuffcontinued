package dev.flarelog.vstuff.internal.rendering;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import dev.flarelog.vstuff.VStuff;

public class RendererParamHelper {

    public static JsonObject blockTexture(String namespace, String name, float scale) {
        return texture(ResourceLocation.fromNamespaceAndPath(namespace, "textures/block/" + name + ".png"), scale);
    }

    public static JsonObject blockTexture(String name, float scale) {
        return texture(ResourceLocation.withDefaultNamespace("textures/block/" + name + ".png"), scale);
    }

    public static JsonObject ropeTexture(String name) {
        return texture(VStuff.asResource("textures/rope/rope_" + name + ".png"));
    }

    public static JsonObject texture(ResourceLocation texture) {
        return texture(texture, 1.0f);
    }

    public static JsonObject texture(ResourceLocation texture, float scale) {
        JsonObject json = new JsonObject();
        json.addProperty("texture", texture.toString());
        json.addProperty("scale", scale);
        return json;
    }

    public static JsonObject color(String hex) {
        JsonObject json = new JsonObject();
        json.addProperty("hex", hex);
        return json;
    }

}
