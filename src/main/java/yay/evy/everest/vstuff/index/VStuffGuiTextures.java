package yay.evy.everest.vstuff.index;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yay.evy.everest.vstuff.VStuff;

/*
Copied from Create
 */
public enum VStuffGuiTextures implements ScreenElement {


    ROPE_MENU("rope_style_changer", 0, 0, 256, 256),
    ROPE_MENU_SCROLL_BAR("rope_scroll", 0, 0, 16, 16),
    ROPE_MENU_SCROLL_BAR_DISABLED("rope_scroll_disabled", 0, 0, 16, 15),
    ;

    public static final int FONT_COLOR = 0x575F7A;

    public final ResourceLocation location;
    public int width, height;
    public int startX, startY;

    private VStuffGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    private VStuffGuiTextures(int startX, int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    private VStuffGuiTextures(String location, int startX, int startY, int width, int height) {
        this(VStuff.MOD_ID, location, startX, startY, width, height);
    }

    private VStuffGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
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