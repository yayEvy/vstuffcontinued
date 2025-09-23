package yay.evy.everest.vstuff.content.rope_changer_menu.components;

import net.minecraft.network.chat.Component;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.ArrayList;
import java.util.List;

public class RopeStyleData {

    static final RopeStyleCategory BASIC_STYLES = new RopeStyleCategory(
            Component.translatable("vstuff.rope_category.normal")
    );

    final RopeStyleCategory CASING_STYLES = new RopeStyleCategory(
            Component.translatable("vstuff.rope_category.casing")
    );

    final RopeStyleCategory FLAGS = new RopeStyleCategory(
            Component.translatable("vstuff.rope_category.flags")
    );


    public static List<RopeStyleCategory> CATEGORIES = new ArrayList<>();

}
