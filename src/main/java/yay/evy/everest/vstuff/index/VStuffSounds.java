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


    private static RegistryObject<SoundEvent> evilSoundRegister(String name) {
        ResourceLocation id = new ResourceLocation(VStuff.MOD_ID, name);

        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
