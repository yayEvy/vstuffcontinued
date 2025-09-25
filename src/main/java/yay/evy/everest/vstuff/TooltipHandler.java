package yay.evy.everest.vstuff;

import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import net.minecraftforge.registries.ForgeRegistries;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffItems;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, value = Dist.CLIENT)
public class TooltipHandler {

    private static final HashMap<Item, Function<SummaryPayload, String>> tooltipModifications = new HashMap<>();

    @SubscribeEvent
    public static void addToItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        if (tooltipModifications.isEmpty()) populateModifiables();

        List<Component> tooltip = event.getToolTip();
        List<Component> tooltipList = new ArrayList<>();

        if (tooltipModifications.containsKey(item)) {
            handleItemTooltip(tooltipList, item);
        }

        tooltip.addAll(1, tooltipList);
    }

    private static void handleItemTooltip(List<Component> tooltipList, Item item) {
        wrapShiftHoldText(tooltipList, "vstuff.tooltip.hold_for_details", () -> {
            Function<SummaryPayload, String> modifier = tooltipModifications.get(item);
            if (modifier != null) {
                String summary = modifier.apply(new SummaryPayload(item));
                tooltipList.addAll(TooltipHelper.cutStringTextComponent(summary, Palette.STANDARD_CREATE));
            }
        });
    }

    private static void wrapShiftHoldText(List<Component> tooltipList, String langKey, Runnable addDetailedContent) {
        boolean isShiftDown = Screen.hasShiftDown();
        Component keyComponent = Component.translatable("create.tooltip.keyShift")
                .withStyle(isShiftDown ? ChatFormatting.WHITE : ChatFormatting.GRAY);

        tooltipList.add(Component.translatable(langKey, keyComponent).withStyle(ChatFormatting.DARK_GRAY));

        if (isShiftDown) {
            tooltipList.add(Component.empty());
            addDetailedContent.run();
        }
    }

    private static void populateModifiables() {
        tooltipModifications.put(VStuffItems.LEAD_CONSTRAINT_ITEM.get(),
                (payload) -> Component.translatable(payload.path + ".tooltip.summary").getString());

        tooltipModifications.put(VStuffItems.LEAD_BREAK_ITEM.get(),
                (payload) -> Component.translatable(payload.path + ".tooltip.summary").getString());
    }

    private static class SummaryPayload {
        public final Item item;
        public final String path;

        public SummaryPayload(Item item) {
            this.item = item;
            this.path = VStuff.MOD_ID + "." + ForgeRegistries.ITEMS.getKey(item).getPath();
        }
    }

}
