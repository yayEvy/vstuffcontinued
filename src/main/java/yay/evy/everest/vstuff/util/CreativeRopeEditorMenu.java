package yay.evy.everest.vstuff.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.index.VStuffMenus;

public class CreativeRopeEditorMenu extends AbstractContainerMenu {

    public final int ropeId;

    public CreativeRopeEditorMenu(int id, Inventory inv, int ropeId) {
        super(VStuffMenus.CREATIVE_ROPE_EDITOR.get(), id);
        this.ropeId = ropeId;
    }

    public CreativeRopeEditorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(VStuffMenus.CREATIVE_ROPE_EDITOR.get(), id);
        this.ropeId = (buf != null) ? buf.readInt() : -1;
    }


    public static CreativeRopeEditorMenu fromBuffer(int id, Inventory inv, FriendlyByteBuf buf) {
        int ropeId = buf.readInt();
        return new CreativeRopeEditorMenu(id, inv, ropeId);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isCreative();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
