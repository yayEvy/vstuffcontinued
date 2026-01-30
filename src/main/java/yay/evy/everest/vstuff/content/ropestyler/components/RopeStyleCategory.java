package yay.evy.everest.vstuff.content.ropestyler.components;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.internal.RopeStyles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RopeStyleCategory {

    private final Component name;
    private final String safeName;
    private final List<RopeStyles.RopeStyle> categoryStyles = new ArrayList<>();

    public RopeStyleCategory(String name, RopeStyles.RopeStyle[] initialContent) {
        this.name = Component.translatable(name);
        this.safeName = name;

        this.categoryStyles.addAll(Arrays.stream(initialContent).toList());
    }

    public RopeStyleCategory(String name) {
        this.name = Component.translatable(name);
        this.safeName = name;
    }

    public @NotNull Component getName() {
        return name;
    }

    public String getNameSafe() {
        return safeName;
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
