package yay.evy.everest.vstuff.index;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.VStuff;

public class VStuffSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VStuff.MOD_ID);

    public static final RegistryObject<SoundEvent> ROPE_THROW = evilSoundRegister("rope_throw");
    public static final RegistryObject<SoundEvent> GRABBER_HUM = evilSoundRegister("grabber_hum");
    public static final RegistryObject<SoundEvent> ASSEMBLE = evilSoundRegister("assemble");

    // levituff sounds
    public static final RegistryObject<SoundEvent> LEVITUFF_GRIND = evilSoundRegister("levituff_grind");
    public static final RegistryObject<SoundEvent> LEVITUFF_D = evilSoundRegister("levituff_d");
    public static final RegistryObject<SoundEvent> LEVITUFF_F = evilSoundRegister("levituff_f");
    public static final RegistryObject<SoundEvent> LEVITUFF_G = evilSoundRegister("levituff_g");
    public static final RegistryObject<SoundEvent> LEVITUFF_A = evilSoundRegister("levituff_a");
    public static final RegistryObject<SoundEvent> LEVITUFF_C = evilSoundRegister("levituff_c");


    private static RegistryObject<SoundEvent> evilSoundRegister(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VStuff.MOD_ID, name);

        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
