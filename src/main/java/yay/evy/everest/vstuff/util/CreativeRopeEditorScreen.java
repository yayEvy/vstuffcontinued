package yay.evy.everest.vstuff.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.ropes.Rope;
import yay.evy.everest.vstuff.content.ropes.RopeTracker;
import yay.evy.everest.vstuff.content.ropestyler.components.RopeStyleButton;
import yay.evy.everest.vstuff.content.ropestyler.components.RopeStyleCategory;
import yay.evy.everest.vstuff.index.VStuffGuiTextures;
import yay.evy.everest.vstuff.index.VStuffRopeStyles;
import yay.evy.everest.vstuff.util.RopeStyles.RopeStyle;
import yay.evy.everest.vstuff.util.client.ClientTextUtils;
import yay.evy.everest.vstuff.util.packet.RopeUpdatePacket;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreativeRopeEditorScreen extends AbstractSimiScreen implements MenuAccess<CreativeRopeEditorMenu> {

    private final CreativeRopeEditorMenu menu;
    private Rope rope;
    private static final VStuffGuiTextures BACKGROUND = VStuffGuiTextures.CREATIVE_ROPE_EDITOR;

    private final List<Component> tabNames = Arrays.asList(
            Component.literal("Rope Styles"),
            Component.literal("Rope Settings")
    );

    private List<RopeStyle> allStyles = new ArrayList<>();
    private RopeStyle[] displayedStyles = new RopeStyle[6];
    private RopeStyleButton[] styleButtons = new RopeStyleButton[6];
    private RopeStyle selectedStyle;

    private ScrollInput lengthInput;
    private Label lengthLabel;
    private Label idLabel;

    private int currentTab = 0;
    private int currentLength;
    private float scrollOffs;

    private Label tabLabel;
    private Label ropeLengthText;
    private Label label;

    public CreativeRopeEditorScreen(CreativeRopeEditorMenu menu, Inventory inv, Component title) {
        super(title);
        this.menu = menu;
        setWindowSize(BACKGROUND.width, BACKGROUND.height);

        VStuffRopeStyles.CATEGORIES.forEach(cat -> allStyles.addAll(cat.getCategoryStyles()));
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        rope = RopeTracker.getActiveRopes().get(menu.ropeId);
        if (rope == null) {
            this.onClose();
            return;
        }

        this.currentLength = (int) rope.maxLength;
        int x = guiLeft;
        int y = guiTop;

        for (int i = 0; i < 6; i++) {
            final int index = i;
            addRenderableWidget(styleButtons[i] =
                    new RopeStyleButton(x + 19, y + 41 + (i * 18), 145, 17, b -> {
                        selectedStyle = displayedStyles[index];
                    })
            );
        }

        tabLabel = new Label(x + 14, y + 25, tabNames.get(currentTab)).withShadow();
        addRenderableWidget(tabLabel);

        ScrollInput tabSelector = new SelectionScrollInput(x + 9, y + 20, 150, 18)
                .forOptions(tabNames)
                .writingTo(tabLabel)
                .setState(currentTab)
                .calling(idx -> {
                    currentTab = idx;
                    refreshTabVisibility();
                });
        addRenderableWidget(tabSelector);

        lengthLabel = new Label(x + 22, y + 64, Component.literal(String.valueOf(currentLength))).withShadow();
        addRenderableWidget(lengthLabel);

        lengthInput = new ScrollInput(x + 19, y + 60, 18, 18)
                .withRange(1, 1024)
                .setState(currentLength)
                .writingTo(lengthLabel)
                .format(val -> Component.literal(String.valueOf(val)))
                .calling(val -> currentLength = val);
        addRenderableWidget(lengthInput);

        IconButton confirmButton = new IconButton(x + BACKGROUND.width - 33, y + BACKGROUND.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onConfirm);
        addRenderableWidget(confirmButton);

        refreshTabVisibility();
    }

    private void refreshTabVisibility() {
        boolean isStyles = (currentTab == 0);

        for (int i = 0; i < styleButtons.length; i++) {
            RopeStyleButton btn = styleButtons[i];
            if (btn != null) {
                btn.visible = isStyles;
                btn.active = isStyles;
                if (!isStyles) {
                    btn.setMessage(Component.empty());
                }
            }
        }

        if (lengthInput != null) {
            lengthInput.visible = !isStyles;
            lengthInput.active = !isStyles;
        }

        if (lengthLabel != null) {
            lengthLabel.visible = !isStyles;
        }

        if (isStyles) {
            setupList(0);
        }
    }

    private void setupList(int offset) {
        for (int i = 0; i < 6; i++) {
            int actualIndex = i + offset;
            if (actualIndex < allStyles.size()) {
                displayedStyles[i] = allStyles.get(actualIndex);
                styleButtons[i].active = true;
            } else {
                displayedStyles[i] = null;
                styleButtons[i].active = false;
            }
        }
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;
        BACKGROUND.render(graphics, x, y);

        MutableComponent title = Component.translatable("vstuff.gui.creative_rope_editor.title");
        graphics.drawString(font, title, x + (BACKGROUND.width / 2) - (font.width(title) / 2), y + 4, 0x582424, false);

        if (currentTab == 0) {
            int scrollBarPos = (int) (41 + (134 - 41) * scrollOffs);
            VStuffGuiTextures barTexture = canScroll() ? VStuffGuiTextures.ROPE_SCROLL : VStuffGuiTextures.ROPE_SCROLL_DISABLED;
            barTexture.render(graphics, x + 11, y + scrollBarPos);

            for (int i = 0; i < 6; i++) {
                RopeStyle style = displayedStyles[i];
                if (style != null) {
                    if (style.getTexture() != null)
                        graphics.blit(style.getTexture(), x + 20, y + 42 + (i * 18), 0, 0, 16, 16, 16, 16);

                    Component name = ClientTextUtils.getComponentWithWidthCutoff(Component.translatable(style.getLangKey()), 114);
                    graphics.drawString(font, name, x + 40, y + 46 + (i * 18), 0xFFFFFF);
                }
            }
        } else {
            String idText = String.valueOf(rope.ID);
            int idWidth = font.width(idText);
            graphics.drawString(font, idText, x + 28 - (idWidth / 2), y + 46, 0xFFFFFF);
            graphics.drawString(font, "Rope ID", x + 40, y + 46, 0xFFFFFF);
            graphics.drawString(font, "Max Length", x + 40, y + 64, 0xFFFFFF);
        }

        if (selectedStyle != null) {
            graphics.drawString(font, Component.translatable(selectedStyle.getLangKey()), x + 15, y + 165, 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int tabX = guiLeft + 9;
        int tabY = guiTop + 20;
        int tabWidth = 150;
        int tabHeight = 18;

        if (mouseX >= tabX && mouseX <= tabX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        if (currentTab == 0 && canScroll()) {
            double listSize = allStyles.size() - 6;
            float scrollFactor = (float) (delta / listSize);

            float oldOffs = scrollOffs;
            scrollOffs = Mth.clamp(scrollOffs - scrollFactor, 0.0F, 1.0F);

            if (oldOffs != scrollOffs) {
                int index = (int) ((scrollOffs * listSize) + 0.5);
                setupList(index);

                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 1.5f + 0.1f * scrollOffs)
                );
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean canScroll() {
        return allStyles.size() > 6;
    }

    private void onConfirm() {
        if (rope == null) return;

        String styleId = (selectedStyle != null) ? selectedStyle.asString() : rope.style.getStyle();
        float length = (float) currentLength;

        NetworkHandler.INSTANCE.sendToServer(new RopeUpdatePacket(rope.ID, length, styleId));

        this.onClose();
    }

    private void sendPreview() {
        if (rope == null) return;
        String styleId = (selectedStyle != null) ? selectedStyle.asString() : rope.style.getStyle();
        NetworkHandler.INSTANCE.sendToServer(new RopeUpdatePacket.RopePreviewPacket(rope.ID, (float) currentLength, styleId));
    }

    @Override
    public CreativeRopeEditorMenu getMenu() {
        return menu;
    }
}
