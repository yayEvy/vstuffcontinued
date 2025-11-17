package yay.evy.everest.vstuff.index;

import com.mojang.blaze3d.systems.RenderSystem;


import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yay.evy.everest.vstuff.VStuff;

/*
Copied from Create
 */
public enum VStuffGuiTextures implements ScreenElement {

    ROPE_STYLER("rope_styler", 0, 0, 182, 184),
    ROPE_SCROLL("rope_styler", 8, 185, 8, 15),
    ROPE_SCROLL_DISABLED("rope_styler", 0, 185, 8, 15)
    ;

    public static final int FONT_COLOR = 0x575F7A;

    public final ResourceLocation location;
    public int width, height;
    public int startX, startY;

    VStuffGuiTextures(String location, int startX, int startY, int width, int height) {
        this(VStuff.MOD_ID, location, startX, startY, width, height);
    }

    VStuffGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @OnlyIn(Dist.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, location);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
    }
}