package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeCategory;

import java.util.HashSet;
import java.util.Set;

import static yay.evy.everest.vstuff.internal.utility.CodecUtil.sanitizeFileName;
import static yay.evy.everest.vstuff.internal.styling.data.RegistryRopeCategory.Builder;

public class VStuffRopeCategories {
    public static final ResourceKey<RegistryRopeCategory> FALLBACK = ResourceKey.create(VStuffRegistries.ROPE_CATEGORIES, VStuff.asResource("uncategorized"));

    public static void bootstrap(BootstapContext<RegistryRopeCategory> ctx) {
        new BootstrapHelper()
                .add(new Builder()
                        .name("Basic Styles")
                        .order(0)
                        .build())
                .add(new Builder()
                        .name("Wool Styles")
                        .order(1)
                        .build())
                .add(new Builder()
                        .name("Color Styles")
                        .order(2)
                        .build())
                .add(new Builder()
                        .name("Pride Styles")
                        .order(3)
                        .build())
                .add(new Builder()
                        .name("Wood Styles")
                        .order(4)
                        .build())
                .add(new Builder()
                        .name("Casing Styles")
                        .order(5)
                        .build())
                .add(new Builder()
                        .name("Merry VStuffmas")
                        .order(6)
                        .build())
                .add(new Builder()
                        .name("Uncategorized")
                        .order(Integer.MAX_VALUE)
                        .build())
                .build(ctx);
    }

    private static class BootstrapHelper {
        private final Set<RegistryRopeCategory> categories;

        BootstrapHelper() {
            this.categories = new HashSet<>();
        }

        BootstrapHelper add(RegistryRopeCategory category) {
            this.categories.add(category);
            return this;
        }

        void build(BootstapContext<RegistryRopeCategory> ctx) {
            for (RegistryRopeCategory category : categories) {
                String name = sanitizeFileName(category.rawName());
                register(ctx, name, category);
            }
        }
    }

    private static void register(BootstapContext<RegistryRopeCategory> ctx, String name, RegistryRopeCategory type) {
        ctx.register(ResourceKey.create(VStuffRegistries.ROPE_CATEGORIES, VStuff.asResource(name)), type);
    }
}
