package yay.evy.everest.vstuff.content.rope_changer_menu.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class RopeStyleButton extends Button {

    public RopeStyleButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    }

    @Override // NO-OP, We take care of rendering ourselves as buttons for text doesn't update properly
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) { }

}
