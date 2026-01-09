package yay.evy.everest.vstuff.index;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.util.CreativeRopeEditorMenu;

public class VStuffMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, VStuff.MOD_ID);

    public static final RegistryObject<MenuType<CreativeRopeEditorMenu>> CREATIVE_ROPE_EDITOR =
            MENUS.register("creative_rope_editor",
                    () -> IForgeMenuType.create((id, inv, buf) -> new CreativeRopeEditorMenu(id, inv, buf))
            );

    public static void register() {
        MENUS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}
