package yay.evy.everest.vstuff.content.rope.roperework;

import com.mojang.blaze3d.platform.Window;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import yay.evy.everest.vstuff.VStuff;

public class RopeTextOverlay implements IGuiOverlay {

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;
        if (NewRopeUtils.hoveringPos == null)
            return;
        if (NewRopeUtils.cached == null || !NewRopeUtils.cached.valid)
            return;
        if (NewRopeUtils.extraTipWarmup < 4)
            return;

        boolean active = mc.options.keySprint.isDown();
        MutableComponent text = VStuff.translate("rope.hold_to_pull_taut", Component.keybind("key.sprint")
                .withStyle(active ? ChatFormatting.WHITE : ChatFormatting.GRAY));

        Window window = mc.getWindow();
        int x = (window.getGuiScaledWidth() - gui.getFont()
                .width(text)) / 2;
        int y = window.getGuiScaledHeight() - 61;
        Color color = new Color(0x4ADB4A).setAlpha(Mth.clamp((NewRopeUtils.extraTipWarmup - 4) / 3f, 0.1f, 1));
        guiGraphics.drawString(gui.getFont(), text, x, y, color.getRGB(), false);
    }
}
