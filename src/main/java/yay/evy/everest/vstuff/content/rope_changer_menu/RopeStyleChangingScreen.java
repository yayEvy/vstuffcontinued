package yay.evy.everest.vstuff.content.rope_changer_menu;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.rope_changer_menu.components.RopeStyleButton;
import yay.evy.everest.vstuff.index.VStuffGuiTextures;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.network.RopeSoundPacket;
import yay.evy.everest.vstuff.network.RopeStyleSelectionPacket;
import yay.evy.everest.vstuff.utils.RopeStyles;
import yay.evy.everest.vstuff.utils.RopeStyles.RopeStyle;
import yay.evy.everest.vstuff.utils.client.ClientTextUtils;

import java.util.Arrays;
import java.util.List;

public class RopeStyleChangingScreen extends AbstractSimiScreen {

    public RopeStyleChangingScreen(Player player) {
        this.player = player;
    }

    private final VStuffGuiTextures background = VStuffGuiTextures.ROPE_MENU;
    RopeStyle[] styleList =
            {
                    new RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.BASIC, "vstuff.ropes.normal"),
                    new RopeStyle("chain", RopeStyles.PrimitiveRopeStyle.CHAIN, "minecraft.block.chain"),
                    new RopeStyle("andesite", RopeStyles.PrimitiveRopeStyle.BASIC, "create.block.andesite_casing"),
                    new RopeStyle("brass", RopeStyles.PrimitiveRopeStyle.BASIC, "create.block.brass_casing"),
                    new RopeStyle("copper", RopeStyles.PrimitiveRopeStyle.BASIC, "create.block.copper_casing"),
                    new RopeStyle("railway", RopeStyles.PrimitiveRopeStyle.BASIC, "create.block.railway_casing"),
                    new RopeStyle("white", RopeStyles.PrimitiveRopeStyle.WOOL, "vstuff.ropes.wool") // white wool as default
            };
    RopeStyle selectedStyle;

    RopeStyle[] displayedStyles = new RopeStyle[6];
    RopeStyleButton[] ropeStyleButtons = new RopeStyleButton[6];
    Player player;


    // Amount scrolled, 0 = top and 1 = bottom
    private float scrollOffs;
    // True if the scrollbar is being dragged
    private boolean scrolling;
    private int ticksOpen;
    private boolean soundPlayed;

    @Override
    protected void init() {
        setWindowSize(background.width, background.height);
        super.init();
        clearWidgets();

        int x = guiLeft;
        int y = guiTop;

        // Initial setup
        setupList();
        selectedStyle = styleList[0];

        // Scrolling Initial setup
        scrollOffs = 0;
        scrollTo(0);

        IconButton closeButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        closeButton.withCallback(this::onMenuClose);
        addRenderableWidget(closeButton);
    }

    @Override
    protected void renderWindow(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack ms = guiGraphics.pose();

        int x = guiLeft;
        int y = guiTop;

        background.render(guiGraphics, x, y);

        MutableComponent header = Component.translatable("vstuff.gui.rope_menu.title");
        int halfWidth = background.width / 2;
        int halfHeaderWidth = font.width(header) / 2;
        guiGraphics.drawString(font, header, x + halfWidth - halfHeaderWidth, y + 4, 0x582424, false);

        ms.pushPose();

        TransformStack msr = TransformStack.cast(ms);
        msr.pushPose()
                .translate(x + background.width + 4, y + background.height + 4, 100)
                .scale(40)
                .rotateX(-22)
                .rotateY(63);

        GuiGameElement.of(VStuffItems.LEAD_CONSTRAINT_ITEM).render(guiGraphics);

        ms.popPose();

        // Render scroll bar
        // Formula is barPos = startLoc + (endLoc - startLoc) * scrollOffs
        int scrollBarPos = (int) (41 + (134 - 41) * scrollOffs);
        VStuffGuiTextures barTexture = canScroll() ? VStuffGuiTextures.ROPE_MENU_SCROLL_BAR : VStuffGuiTextures.ROPE_MENU_SCROLL_BAR_DISABLED;
        barTexture.render(guiGraphics, x + 11, y + scrollBarPos);

        // Render the bogey icons & bogey names
        for (RopeStyle style : styleList) {
            ResourceLocation icon = style.getTexture();
            if (icon != null)
                renderIcon(guiGraphics, ms, icon, x + 20, y + 42);

            // Text
            Component bogeyName = ClientTextUtils.getComponentWithWidthCutoff(Component.translatable(style.getLangKey()), 55);
            // button has already been added in init, now just draw text
            guiGraphics.drawString(font, bogeyName, x + 40, y + 46 , 0xFFFFFF);

        }

        // Draw bogey name, gauge indicators and render bogey
        if (selectedStyle != null) {
            Minecraft mc = Minecraft.getInstance();
            Component displayName = Component.translatable(selectedStyle.getLangKey());
            // Bogey Name
            Component bogeyName = ClientTextUtils.getComponentWithWidthCutoff(displayName, 126);
            guiGraphics.drawCenteredString(font, bogeyName, x + 190, y + 25, 0xFFFFFF);

            ResourceLocation styleTexture = selectedStyle.getTexture();


            ms.popPose();

            // Clear depth rectangle to allow proper tooltips
            {
                double x0 = x + 120;
                double y0 = y + 48;
                double w = 140;
                double h = 77;
                double bottom = y0+h;

                Window window = mc.getWindow();
                double scale = window.getGuiScale();

                RenderSystem.clearDepth(0.86); // same depth as gui
                RenderSystem.enableScissor((int) (x0*scale), window.getHeight() - (int) (bottom*scale), (int) (w*scale), (int) (h*scale));

                RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);

                RenderSystem.disableScissor();
                RenderSystem.clearDepth(1.0);
            }
        }
    }

    private void renderIcon(GuiGraphics guiGraphics, PoseStack ms, ResourceLocation icon, int x, int y) {
        ms.pushPose();
        guiGraphics.blit(icon, x, y, 0, 0, 0, 16, 16, 16, 16);
        ms.popPose();
    }

    private void setupList() {
        setupList(0);
    }

    private void setupList(int offset) {

        // Max of 6 slots, objects inside the slots will be mutated later
        for (int i = 0; i < 6; i++) {
            if (i < styleList.length) {
                displayedStyles[i] = styleList[i+offset];
            } else {
                // I know, this is silly but its best way to know if rendering should be skipped
                displayedStyles[i] = null;
            }
        }
    }

    @Override
    public void tick() {
        ticksOpen++;
        soundPlayed = false;
        super.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (selectedStyle == null)
                onClose();
            else
                onMenuClose();

            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            onMenuClose();

            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (insideScrollbar(mouseX, mouseY)) {
                scrolling = canScroll();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0)
            scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!scrolling) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        int scrollbarLeft = guiTop + 41;
        int scrollbarRight = scrollbarLeft + 108;
        float scrollFactor = (float) ((mouseY - scrollbarLeft - 7.5F) / (scrollbarRight - scrollbarLeft - 15.0F));
        scrollOffs = Mth.clamp(scrollFactor, 0.0F, 1.0F);
        scrollTo(scrollOffs);

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        super.mouseScrolled(mouseX, mouseY, delta);
        if (!canScroll()) return false;
        if (insideCategorySelector(mouseX, mouseY)) return false;

        double listSize = styleList.length - 6;
        float scrollFactor = (float) (delta / listSize);

        final float oldScrollOffs = scrollOffs;

        scrollOffs = Mth.clamp(scrollOffs - scrollFactor, 0.0F, 1.0F);
        scrollTo(scrollOffs);

        return true;
    }

    private void scrollTo(float pos) {
        float listSize = styleList.length - 6;
        int index = (int) ((double) (pos * listSize) + 0.5);

        setupList(index);
    }

    private boolean canScroll() {
        return styleList.length > 6;
    }

    private boolean insideCategorySelector(double mouseX, double mouseY) {
        int scrollbarLeftX = guiLeft + 11;
        int scrollbarTopY = guiTop + 20;
        int scrollbarRightX = scrollbarLeftX + 90;
        int scrollbarBottomY = scrollbarTopY + 34;

        return mouseX >= scrollbarLeftX && mouseY >= scrollbarTopY && mouseX < scrollbarRightX && mouseY < scrollbarBottomY;
    }

    private boolean insideScrollbar(double mouseX, double mouseY) {
        int scrollbarLeftX = guiLeft + 11;
        int scrollbarTopY = guiTop + 41;
        int scrollbarRightX = scrollbarLeftX + 8;
        int scrollbarBottomY = scrollbarTopY + 108;

        return mouseX >= scrollbarLeftX && mouseY >= scrollbarTopY && mouseX < scrollbarRightX && mouseY < scrollbarBottomY;
    }

    private Button.OnPress styleSelection(int index) {
        return b -> {
            selectedStyle = styleList[index];
        };
    }

    private void onMenuClose() {
        if (selectedStyle == null) return;

        RopeStyle style = selectedStyle;

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new RopeStyleSelectionPacket(style)
            );
        }

        onClose();
    }

}
