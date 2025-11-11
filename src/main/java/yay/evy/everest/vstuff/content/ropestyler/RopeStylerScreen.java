package yay.evy.everest.vstuff.content.ropestyler;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.world.entity.player.Player;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.ropestyler.components.RopeStyleCategory;
import yay.evy.everest.vstuff.index.VStuffRopeStyles;
import yay.evy.everest.vstuff.util.RopeStyles.RopeStyle;
import yay.evy.everest.vstuff.util.client.ClientTextUtils;
import yay.evy.everest.vstuff.index.VStuffGuiTextures;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.content.ropestyler.components.RopeStyleButton;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import yay.evy.everest.vstuff.util.packet.RopeStyleSelectPacket;

import java.util.List;

public class RopeStylerScreen extends AbstractSimiScreen {

    public RopeStylerScreen(Player player) {
        this.player = player;
    }
    Player player;

    private final VStuffGuiTextures background = VStuffGuiTextures.ROPE_STYLER;
    // The names of bogey categories
    private final List<Component> categoryComponentList = VStuffRopeStyles.CATEGORIES.stream()
            .map(RopeStyleCategory::getName)
            .toList();
    // The category that is currently selected
    private RopeStyleCategory selectedCategory = VStuffRopeStyles.CATEGORIES.get(0);
    private int categoryIndex = 0; // for the scroll input on window resize
    // The list of bogies being displayed
    RopeStyle[] displayedStyles = new RopeStyle[6];
    // The list of bogey selection buttons
    RopeStyleButton[] styleButtons = new RopeStyleButton[6];
    // The bogey that is currently selected
    RopeStyle selectedStyle;
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

        // Need buttons first, otherwise setupList will crash
        for (int i = 0; i < 6; i++) {
            addRenderableWidget(styleButtons[i] = new RopeStyleButton(x + 19, y + 41 + (i * 18), 145, 17, bogeySelection(i)));
        }

        // Initial setup
        setupList(selectedCategory);
        selectedStyle = selectedCategory.getCategoryStyles().get(0);

        // Scrolling Initial setup
        scrollOffs = 0;
        scrollTo(0);

        // category select
        Label categoryLabel = new Label(x + 14, y + 25, Component.empty()).withShadow();
        ScrollInput categoryScrollInput = new SelectionScrollInput(x + 9, y + 20, 150, 18)
                .forOptions(categoryComponentList)
                .writingTo(categoryLabel)
                .setState(categoryIndex)
                .calling(categoryIndex -> {
                    scrollOffs = 0.0F;
                    scrollTo(0.0F);
                    this.categoryIndex = categoryIndex;
                    setupList(selectedCategory = VStuffRopeStyles.CATEGORIES.get(categoryIndex));

                    if (!selectedCategory.getCategoryStyles().isEmpty()) {
                        selectedStyle = selectedCategory.getCategoryStyles().get(0);
                    } else {
                        selectedStyle = null;
                    }
                });

        addRenderableWidget(categoryLabel);
        addRenderableWidget(categoryScrollInput);

        IconButton closeButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        closeButton.withCallback(this::onMenuClose);
        addRenderableWidget(closeButton);
    }

    @Override
    protected void renderWindow(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack ms = guiGraphics.pose();

        int x = guiLeft;
        int y = guiTop;

        // bg
        background.render(guiGraphics, x, y);

        // header
        MutableComponent header = Component.translatable("vstuff.gui.rope_menu.title");
        int halfWidth = background.width / 2;
        int halfHeaderWidth = font.width(header) / 2;
        guiGraphics.drawString(font, header, x + halfWidth - halfHeaderWidth, y + 4, 0x582424, false);



        // Render scroll bar
        // Formula is barPos = startLoc + (endLoc - startLoc) * scrollOffs
        int scrollBarPos = (int) (41 + (134 - 41) * scrollOffs);
        VStuffGuiTextures barTexture = canScroll() ? VStuffGuiTextures.ROPE_SCROLL : VStuffGuiTextures.ROPE_SCROLL_DISABLED;
        barTexture.render(guiGraphics, x + 11, y + scrollBarPos);

        // render names and textures
        for (int i = 0; i < 6; i++) {
            RopeStyle style = displayedStyles[i];
            if (style != null) {
                // texture
                ResourceLocation icon = style.getTexture();
                if (icon != null)
                    renderIcon(guiGraphics, ms, icon, x + 20, y + 42 + (i * 18));

                // name
                Component bogeyName = ClientTextUtils.getComponentWithWidthCutoff(Component.translatable(style.getLangKey()), 114);
                // button has already been added in init, now just draw text
                guiGraphics.drawString(font, bogeyName, x + 40, y + 46 + (i * 18), 0xFFFFFF);
            }
        }

        // show name of selected
        if (selectedStyle != null) {
            Component displayName = Component.translatable(selectedStyle.getLangKey());
            Component shortenedName = ClientTextUtils.getComponentWithWidthCutoff(displayName, 126);
            guiGraphics.drawString(font, shortenedName, x + 15, y + 165, 0xFFFFFF);

           // ms.popPose();
        }
    }

    private void renderIcon(GuiGraphics guiGraphics, PoseStack ms, ResourceLocation icon, int x, int y) {
        ms.pushPose();
        guiGraphics.blit(icon, x, y, 0, 0, 0, 16, 16, 16, 16);
        ms.popPose();
    }

    private void setupList(RopeStyleCategory categoryEntry) {
        setupList(categoryEntry, 0);
    }

    private void setupList(RopeStyleCategory categoryEntry, int offset) {
        List<RopeStyle> styles = categoryEntry.getCategoryStyles();

        for (int i = 0; i < 6; i++) {
            if (i < styles.size()) {
                displayedStyles[i] = styles.get(i+offset);
                styleButtons[i].active = true;
            } else {
                displayedStyles[i] = null;
                styleButtons[i].active = false;
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
        if (selectedCategory.getCategoryStyles().size() < 6) return false;

        double listSize = selectedCategory.getCategoryStyles().size() - 6;
        float scrollFactor = (float) (delta / listSize);

        final float oldScrollOffs = scrollOffs;

        scrollOffs = Mth.clamp(scrollOffs - scrollFactor, 0.0F, 1.0F);
        scrollTo(scrollOffs);

        if (!soundPlayed && scrollOffs != oldScrollOffs)
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(),
                            1.5f + 0.1f * scrollOffs));
        soundPlayed = true;

        return true;
    }

    private void scrollTo(float pos) {
        List<RopeStyle> styles = selectedCategory.getCategoryStyles();
        float listSize = styles.size() - 6;
        int index = (int) ((double) (pos * listSize) + 0.5);

        setupList(selectedCategory, index);
    }

    private boolean canScroll() {
        return selectedCategory.getCategoryStyles().size() > 6;
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

    private Button.OnPress bogeySelection(int index) {
        return b -> {
            selectedStyle = displayedStyles[index];
        };
    }

    private void onMenuClose() {
        if (selectedStyle == null) return;

        RopeStyle style = selectedStyle;

        VStuff.LOGGER.info("Attempting to set player [{}] selected rope style to {}", player.getName(), selectedStyle.asString());

        NetworkHandler.INSTANCE.sendToServer(new RopeStyleSelectPacket(style.asString()));

        onClose();
    }

}