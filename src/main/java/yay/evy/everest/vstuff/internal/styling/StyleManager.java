package yay.evy.everest.vstuff.internal.styling;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeCategory;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeStyle;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StyleManager {

    public static LinkedHashMap<RegistryRopeCategory, List<RegistryRopeStyle>> getCategoriesWithStyles(RegistryAccess regAccess) {
        Registry<RegistryRopeStyle> styleReg = regAccess.registryOrThrow(VStuffRegistries.ROPE_STYLES);
        Registry<RegistryRopeCategory> categoryReg = regAccess.registryOrThrow(VStuffRegistries.ROPE_CATEGORIES);

        List<RegistryRopeCategory> categories = categoryReg.stream().sorted(Comparator.comparingInt(RegistryRopeCategory::order)).toList();

        LinkedHashMap<RegistryRopeCategory, List<RegistryRopeStyle>> categoryWithStylesMap = new LinkedHashMap<>();

        for (RegistryRopeCategory category : categories) {
            ResourceLocation categoryKey = categoryReg.getKey(category); // resource location of the category
            List<RegistryRopeStyle> stylesForCategory = styleReg
                .stream()
                .filter(style -> style.categories()
                    .stream()
                    .anyMatch(h -> h.is(categoryKey))
                ) // filter styles by if any of their categories match the given resource location
                .toList();

            categoryWithStylesMap.put(category, stylesForCategory);
        }

        return categoryWithStylesMap;
    }

}
