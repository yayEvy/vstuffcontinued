package yay.evy.everest.vstuff.content.rope_changer_menu;

import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class RopeStyleChangingMenu extends MenuBase<RopeStyleDisplaySupplier> {

    public RopeStyleChangingMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    @Override
    protected RopeStyleDisplaySupplier createOnClient(FriendlyByteBuf extraData) {
        return null;
    }

    @Override
    protected void initAndReadInventory(RopeStyleDisplaySupplier contentHolder) {

    }


    @Override
    protected void addSlots() {

    }

    @Override
    protected void saveData(RopeStyleDisplaySupplier contentHolder) {

    }


    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }
}
