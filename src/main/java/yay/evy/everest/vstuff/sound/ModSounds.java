package yay.evy.everest.vstuff.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.vstuff; // your main mod class

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, vstuff.MOD_ID);

    public static final RegistryObject<SoundEvent> ROPE_CREATE =
            SOUND_EVENTS.register("rope_create",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(vstuff.MOD_ID, "rope_create")));
    public static final RegistryObject<SoundEvent> ROPE_BREAK =
            SOUND_EVENTS.register("rope_break",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(vstuff.MOD_ID, "rope_break")));
}
