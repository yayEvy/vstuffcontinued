package yay.evy.everest.vstuff.index;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import yay.evy.everest.vstuff.VStuff;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum VStuffKeys {

    ROPE_MENU("rope_menu_open", GLFW.GLFW_KEY_LEFT_ALT, "Open Rope Styling Menu"),
    GRABBER_DISTANCE("phys_grabber_distance_change", GLFW.GLFW_KEY_LEFT_ALT, "Phys Grabber Distance Changing")
    ;

    private KeyMapping keybind;
    private final String description;
    private final String translation;
    private final int key;
    private final boolean modifiable;

    VStuffKeys(String description, int defaultKey, String translation) {
        this.description = VStuff.MOD_ID + ".keyinfo." + description;
        this.key = defaultKey;
        this.modifiable = !description.isEmpty();
        this.translation = translation;
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
        for (VStuffKeys key : values())
            if (key.modifiable)
                consumer.accept(key.description, key.translation);
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        for (VStuffKeys key : values()) {
            key.keybind = new KeyMapping(key.description, key.key, VStuff.NAME);

            event.register(key.keybind);
        }
    }

    public boolean isPressed() {
        return keybind.isDown();
    }

    public int getBoundCode() {
        return keybind.getKey()
                .getValue();
    }
}
