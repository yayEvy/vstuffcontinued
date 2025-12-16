package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropethrower.RopeThrowerEntity;

public class VStuffEntities {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    public static final EntityEntry<RopeThrowerEntity> ROPE_THROWER =
            REGISTRATE.entity("rope_thrower", RopeThrowerEntity::new, MobCategory.MISC)
                    .properties(p -> p.sized(0.5f, 0.5f).build("rope_thrower"))
                    .register();

    public static void register(IEventBus eventBus) {}

}