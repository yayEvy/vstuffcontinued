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

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum VStuffKeys {

    ROPE_MENU("rope_menu_open", GLFW.GLFW_KEY_LEFT_ALT);

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
