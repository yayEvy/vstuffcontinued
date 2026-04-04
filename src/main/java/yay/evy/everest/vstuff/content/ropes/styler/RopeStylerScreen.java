package yay.evy.everest.vstuff.content.ropes.styler;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.client.rope.RopeRendererType;
import yay.evy.everest.vstuff.client.rope.RopeRendererTypes;
import yay.evy.everest.vstuff.content.ropes.type.RopeCategory;
import yay.evy.everest.vstuff.content.ropes.type.RopeType;
import yay.evy.everest.vstuff.content.ropes.type.RopeTypeRegistry;
import yay.evy.everest.vstuff.internal.RopeStyleCategoryManager;
import yay.evy.everest.vstuff.internal.RopeStyleManager;
import yay.evy.everest.vstuff.internal.network.NetworkHandler;
import yay.evy.everest.vstuff.internal.utility.ClientTextUtils;
import yay.evy.everest.vstuff.index.VStuffGuiTextures;

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

import java.util.List;

public class RopeStylerScreen extends AbstractSimiScreen {

    public RopeStylerScreen(Player player) {
        this.player = player;
    }

    Player player;

    private final VStuffGuiTextures background = VStuffGuiTextures.ROPE_STYLER;

    private RopeCategory selectedCategory;

    private int categoryIndex = 0;

    RopeType[] displayedTypes = new RopeType[6];

    RopeTypeButton[] styleButtons = new RopeTypeButton[6];

    RopeType selectedType;

    private float scrollOffs;

    private boolean scrolling;

    private boolean soundPlayed;

    @Override
    protected void init() {
        setWindowSize(background.width, background.height);
        super.init();
        clearWidgets();
        
        List<RopeCategory> categories = RopeTypeRegistry.buildSortedCategories();
        List<Component> categoryComponentList = categories.stream().map(RopeCategory::name).toList();

        int x = guiLeft;
        int y = guiTop;

        for (int i = 0; i < 6; i++) {
            addRenderableWidget(styleButtons[i] = new RopeTypeButton(x + 19, y + 41 + (i * 18), 145, 17, bogeySelection(i)));
        }

        if (categories.isEmpty()) {
            selectedCategory = null;
            selectedType = null;
            return;
        }

        selectedCategory = categories.get(0);

        setupList(selectedCategory);

        if (!selectedCategory.types().isEmpty()) {
            selectedType = selectedCategory.types().get(0);
        } else {
            selectedType = null;
        }

        scrollOffs = 0;
        scrollTo(0);

        Label categoryLabel = new Label(x + 14, y + 25, Component.empty()).withShadow();
        ScrollInput categoryScrollInput = new SelectionScrollInput(x + 9, y + 20, 150, 18)
                .forOptions(categoryComponentList)
                .writingTo(categoryLabel)
                .setState(categoryIndex)
                .calling(categoryIndex -> {
                    scrollOffs = 0.0F;
                    scrollTo(0.0F);
                    this.categoryIndex = categoryIndex;

                    if (categories.isEmpty()) {
                        selectedCategory = null;
                        selectedType = null;
                        return;
                    }

                    selectedCategory = categories.get(categoryIndex);
                    setupList(selectedCategory);

                    if (!selectedCategory.types().isEmpty()) {
                        selectedType = selectedCategory.types().get(0);
                    } else {
                        selectedType = null;
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

        if (selectedCategory == null) {
            background.render(guiGraphics, guiLeft, guiTop);
            return;
        }

        background.render(guiGraphics, x, y);

        MutableComponent header = Component.translatable("vstuff.gui.rope_menu.title");
        int halfWidth = background.width / 2;
        int halfHeaderWidth = font.width(header) / 2;
        guiGraphics.drawString(font, header, x + halfWidth - halfHeaderWidth, y + 4, 0x582424, false);


        int scrollBarPos = (int) (41 + (134 - 41) * scrollOffs);
        VStuffGuiTextures barTexture = canScroll() ? VStuffGuiTextures.ROPE_SCROLL : VStuffGuiTextures.ROPE_SCROLL_DISABLED;
        barTexture.render(guiGraphics, x + 11, y + scrollBarPos);

        for (int i = 0; i < 6; i++) {
            RopeType style = displayedTypes[i];
            if (style != null) {
                    renderIcon(guiGraphics, ms, style, x + 20, y + 42 + (i * 18));
                Component styleName = ClientTextUtils.getComponentWithWidthCutoff(style.name(), 114);
                guiGraphics.drawString(font, styleName, x + 40, y + 46 + (i * 18), 0xFFFFFF);
            }
        }

        if (selectedType != null) {
            Component displayName = selectedType.name();
            Component shortenedName = ClientTextUtils.getComponentWithWidthCutoff(displayName, 126);
            guiGraphics.drawString(font, shortenedName, x + 15, y + 165, 0xFFFFFF);

        }
    }

    private void renderIcon(GuiGraphics guiGraphics, PoseStack ms, RopeType type, int x, int y) {
        RopeRendererType rendererType = RopeRendererTypes.get(type.rendererTypeId());
        if (rendererType == null) return;

        ms.pushPose();

        ms.translate(x, y, 0);

        rendererType.renderPreview(guiGraphics, type.rendererParams());

        ms.popPose();
    }

    private void setupList(RopeCategory categoryEntry) {
        setupList(categoryEntry, 0);
    }

    private void setupList(RopeCategory categoryEntry, int offset) {
        List<RopeType> styles = categoryEntry.types();

        for (int i = 0; i < 6; i++) {
            if (i + offset < styles.size()) {
                displayedTypes[i] = styles.get(i + offset);
                styleButtons[i].active = true;
            } else {
                displayedTypes[i] = null;
                styleButtons[i].active = false;
            }
        }
    }

    @Override
    public void tick() {
        soundPlayed = false;
        super.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (selectedType == null)
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
        if (selectedCategory == null || selectedCategory.types().size() < 6) return false;

        double listSize = selectedCategory.types().size() - 6;
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
        if (selectedCategory == null) return;

        List<RopeType> styles = selectedCategory.types();
        float listSize = styles.size() - 6;
        int index = (int) ((double) (pos * listSize) + 0.5);

        setupList(selectedCategory, index);
    }

    private boolean canScroll() {
        return selectedCategory != null && selectedCategory.types().size() > 6;
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
        return b -> selectedType = displayedTypes[index];
    }

    private void onMenuClose() {
        if (selectedType == null) return;

        NetworkHandler.selectType(selectedType.id());

        onClose();
    }

    public static class RopeTypeButton extends Button {

        public RopeTypeButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) { }

    }


}