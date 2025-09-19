package yay.evy.everest.vstuff.index;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.VStuff; // your main mod class

public class VStuffSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VStuff.MOD_ID);

    public static final RegistryObject<SoundEvent> ROPE_CREATE = easySoundRegister("rope_create");
    public static final RegistryObject<SoundEvent> ROPE_BREAK = easySoundRegister("rope_break");


    private static RegistryObject<SoundEvent> easySoundRegister(String name) {
        ResourceLocation id = new ResourceLocation(VStuff.MOD_ID, name);

        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
