package dev.flarelog.vstuff.index;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import dev.flarelog.vstuff.VStuff;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum VStuffKeys {

    ROPE_MENU("rope_menu_open", GLFW.GLFW_KEY_LEFT_ALT),
    GRABBER_DISTANCE("phys_grabber_distance_change", GLFW.GLFW_KEY_LEFT_ALT)
    ;

    private KeyMapping keybind;
    private final String description;
    private int key;

    VStuffKeys(String description, int defaultKey) {
        this.description = VStuff.MOD_ID + ".keyinfo." + description;
        this.key = defaultKey;
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
