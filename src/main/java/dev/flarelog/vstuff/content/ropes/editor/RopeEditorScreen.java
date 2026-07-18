package dev.flarelog.vstuff.content.ropes.editor;

import dev.flarelog.vstuff.content.ropes.phys_ropes.ReworkedPhysRope;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;

public class RopeEditorScreen extends AbstractSimiScreen {

    public RopeEditorScreen(ReworkedPhysRope rope) {
        this.rope = rope;
    }

    ReworkedPhysRope rope;

    @Override
    protected void renderWindow(GuiGraphics guiGraphics, int i, int i1, float v) { // window of doom and despair

    }
}
