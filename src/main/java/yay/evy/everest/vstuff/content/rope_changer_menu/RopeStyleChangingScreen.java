package yay.evy.everest.vstuff.content.rope_changer_menu;

import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RopeStyleChangingScreen extends AbstractSimiContainerScreen {

    public RopeStyleChangingScreen(RopeStyleChangingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
