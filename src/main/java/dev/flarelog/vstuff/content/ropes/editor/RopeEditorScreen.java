package dev.flarelog.vstuff.content.ropes.editor;

import dev.flarelog.vstuff.content.ropes.Rope;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;

public class RopeEditorScreen extends AbstractSimiScreen {

    public RopeEditorScreen(Rope rope) {
        this.rope = rope;
    }

    Rope rope;

    @Override
    protected void renderWindow(GuiGraphics guiGraphics, int i, int i1, float v) { // window of doom and despair

    }
}
