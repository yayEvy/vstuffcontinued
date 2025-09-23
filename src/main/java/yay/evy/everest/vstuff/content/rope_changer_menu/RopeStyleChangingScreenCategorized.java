package yay.evy.everest.vstuff.content.rope_changer_menu;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.rope_changer_menu.components.RopeStyleCategory;
import yay.evy.everest.vstuff.content.rope_changer_menu.components.RopeStyleData;
import yay.evy.everest.vstuff.util.RopeStyles;
import yay.evy.everest.vstuff.util.RopeStyles.RopeStyle;
import yay.evy.everest.vstuff.util.client.ClientTextUtils;
import yay.evy.everest.vstuff.index.VStuffGuiTextures;
import yay.evy.everest.vstuff.content.rope_changer_menu.handler.RopeStyleMenuHandler;
import yay.evy.everest.vstuff.content.rope_changer_menu.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.content.rope_changer_menu.components.RopeStyleButton;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.bogey.BogeyStyle;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.*;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RopeStyleChangingScreenCategorized extends AbstractSimiScreen {

    public RopeStyleChangingScreenCategorized(Player player) {
        this.player = player;
    }
    Player player;

    private final VStuffGuiTextures background = VStuffGuiTextures.ROPE_MENU;
    // The names of bogey categories
    private final List<Component> categoryComponentList = RopeStyleData.CATEGORIES.stream()
            .map(RopeStyleCategory::getName)
            .toList();
    // The category that is currently selected
    private RopeStyleCategory selectedCategory = RopeStyleData.CATEGORIES.get(0);
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
            addRenderableWidget(styleButtons[i] = new RopeStyleButton(x + 19, y + 41 + (i * 18), 82, 17, bogeySelection(i)));
        }

        // Initial setup
        setupList(selectedCategory);
        selectedStyle = selectedCategory.getCategoryStyles().get(0);

        // Scrolling Initial setup
        scrollOffs = 0;
        scrollTo(0);

        // Category selector START
        Label categoryLabel = new Label(x + 14, y + 25, Components.immutableEmpty()).withShadow();
        ScrollInput categoryScrollInput = new SelectionScrollInput(x + 9, y + 20, 77, 18)
                .forOptions(categoryComponentList)
                .writingTo(categoryLabel)
                .setState(categoryIndex)
                .calling(categoryIndex -> {
                    scrollOffs = 0.0F;
                    scrollTo(0.0F);
                    this.categoryIndex = categoryIndex;
                    setupList(selectedCategory = RopeStyleData.CATEGORIES.get(categoryIndex));

                    if (!selectedCategory.getCategoryStyles().isEmpty()) {
                        selectedStyle = selectedCategory.getCategoryStyles().get(0);
                    } else {
                        selectedStyle = null;
                    }
                });

        addRenderableWidget(categoryLabel);
        addRenderableWidget(categoryScrollInput);

        // Close Button
        IconButton closeButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        closeButton.withCallback(this::onMenuClose);
        addRenderableWidget(closeButton);
    }

    @Override
    protected void renderWindow(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack ms = guiGraphics.pose();

        int x = guiLeft;
        int y = guiTop;

        // Render Background
        background.render(guiGraphics, x, y);

        // Header (Bogey Preview Text) START
        MutableComponent header = Component.translatable("vstuff.gui.rope_menu.title");
        int halfWidth = background.width / 2;
        int halfHeaderWidth = font.width(header) / 2;
        guiGraphics.drawString(font, header, x + halfWidth - halfHeaderWidth, y + 4, 0x582424, false);

        // Train casing on right side of screen where arrow is pointing START
        ms.pushPose();

        TransformStack msr = TransformStack.cast(ms);
        msr.pushPose()
                .translate(x + background.width + 4, y + background.height + 4, 100)
                .scale(40)
                .rotateX(-22)
                .rotateY(63);

        GuiGameElement.of(AllBlocks.RAILWAY_CASING.getDefaultState()).render(guiGraphics);

        ms.popPose();

        // Render scroll bar
        // Formula is barPos = startLoc + (endLoc - startLoc) * scrollOffs
        int scrollBarPos = (int) (41 + (134 - 41) * scrollOffs);
        VStuffGuiTextures barTexture = canScroll() ? VStuffGuiTextures.ROPE_MENU_SCROLL_BAR : VStuffGuiTextures.ROPE_MENU_SCROLL_BAR_DISABLED;
        barTexture.render(guiGraphics, x + 11, y + scrollBarPos);

        // Render the bogey icons & bogey names
        for (int i = 0; i < 6; i++) {
            RopeStyle style = displayedStyles[i];
            if (style != null) {
                // Icon
                ResourceLocation icon = style.getTexture();
                if (icon != null)
                    renderIcon(guiGraphics, ms, icon, x + 20, y + 42 + (i * 18));

                // Text
                Component bogeyName = ClientTextUtils.getComponentWithWidthCutoff(Component.translatable(style.getLangKey()), 55);
                // button has already been added in init, now just draw text
                guiGraphics.drawString(font, bogeyName, x + 40, y + 46 + (i * 18), 0xFFFFFF);
            }
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

    private void setupList(RopeStyleCategory categoryEntry) {
        setupList(categoryEntry, 0);
    }

    private void setupList(RopeStyleCategory categoryEntry, int offset) {
        List<RopeStyle> bogies = categoryEntry.getCategoryStyles();

        // Max of 6 slots, objects inside the slots will be mutated later
        for (int i = 0; i < 6; i++) {
            if (i < bogies.size()) {
                displayedStyles[i] = bogies.get(i+offset);
                styleButtons[i].active = true;
            } else {
                // I know, this is silly but its best way to know if rendering should be skipped
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

        System.out.println("Attempting to set player [" + player.getName() + "] selected rope style to " + selectedStyle.asString());

        RopeStyleHandlerServer.addStyle(player.getUUID(), style);

        onClose();
    }
}