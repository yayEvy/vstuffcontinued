package yay.evy.everest.vstuff.internal.rendering;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import static yay.evy.everest.vstuff.VStuff.asResource;
import static yay.evy.everest.vstuff.VStuff.mcResource;

public class RendererParamHelper {

    public static class ColorParamBuilder {
        private String hex;
        private int color;

        public ColorParamBuilder hex(String hex) {
            this.hex = hex;
            return this;
        }

        public ColorParamBuilder color(int color) {
            this.color = color;
            return this;
        }

        public JsonObject build() {
            JsonObject json = new JsonObject();
            if (hex == null) json.addProperty("color", color);
            else json.addProperty("hex", hex);
            return json;
        }
    }

    public static class TextureParamBuilder {
        private ResourceLocation texture = VStuff.asResource("textures/rope/rope_normal.png");
        private float scale = 1.0f;

        public TextureParamBuilder ropeTexture(String name) {
            return ropeTexture(VStuff.MOD_ID, "textures/rope/rope_" + name + ".png");
        }

        public TextureParamBuilder ropeTexture(String namespace, String name) {
            return texture(ResourceLocation.fromNamespaceAndPath(namespace, name));
        }

        public TextureParamBuilder blockTexture(String namespace, String name) {
            return texture(ResourceLocation.fromNamespaceAndPath(namespace, "textures/block/" + name + ".png"));
        }

        public TextureParamBuilder itemTexture(String namespace, String name) {
            return texture(ResourceLocation.fromNamespaceAndPath(namespace, "textures/item/" + name + ".png"));
        }

        public TextureParamBuilder blockTexture(String name) {
            return blockTexture("minecraft", name);
        }

        public TextureParamBuilder itemTexture(String name) {
            return itemTexture("minecraft", name);
        }

        public TextureParamBuilder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public TextureParamBuilder halfSize() {
            return scale(0.5f);
        }

        public TextureParamBuilder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public JsonObject build() {
            JsonObject json = new JsonObject();
            json.addProperty("texture", texture.toString());
            json.addProperty("scale", scale);
            return json;
        }
    }

    public static int parseColour(String hex) {
        String stripped = hex.startsWith("#") ? hex.substring(1) : hex;
        if (stripped.length() == 6) {
            // No alpha, prepend FF
            stripped = "FF" + stripped;
        }
        return (int) Long.parseLong(stripped, 16);
    }

}
