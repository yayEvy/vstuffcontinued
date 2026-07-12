package dev.flarelog.vstuff.content.ropes.editor;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import dev.flarelog.vstuff.content.ropes.ReworkedRope;

public class RopeEditorScreen extends AbstractSimiScreen {

    public RopeEditorScreen(ReworkedRope rope) {
        this.rope = rope;
    }

    ReworkedRope rope;

    @Override
    protected void renderWindow(GuiGraphics guiGraphics, int i, int i1, float v) { // window of doom and despair

    }
}
