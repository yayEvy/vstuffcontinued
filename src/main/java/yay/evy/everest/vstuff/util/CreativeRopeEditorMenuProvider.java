package yay.evy.everest.vstuff.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class CreativeRopeEditorMenuProvider implements MenuProvider {

    private final int ropeId;

    public CreativeRopeEditorMenuProvider(int ropeId) {
        this.ropeId = ropeId;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("vstuff.gui.creative_rope_editor.title");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CreativeRopeEditorMenu(id, inv, ropeId);
    }
}
