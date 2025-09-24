package yay.evy.everest.vstuff.content.rope_changer_menu.components;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RopeStyleCategory {

    private final Component name;
    private final List<RopeStyles.RopeStyle> categoryStyles = new ArrayList<>();

    public RopeStyleCategory(Component name, RopeStyles.RopeStyle[] initialContent) {
        this.name = name;

        this.categoryStyles.addAll(Arrays.stream(initialContent).toList());
    }

    public RopeStyleCategory(Component name) {
        this.name = name;
    }

    public @NotNull Component getName() {
        return name;
    }

    public @NotNull List<RopeStyles.RopeStyle> getCategoryStyles() {
        return categoryStyles;
    }

    public void addStyle(RopeStyles.RopeStyle style) {
        this.categoryStyles.add(style);
    }

    public void addStyle(String style, RopeStyles.PrimitiveRopeStyle basicStyle, String styleLKey) {
        this.addStyle(new RopeStyles.RopeStyle(style, basicStyle, styleLKey));
    }

}
