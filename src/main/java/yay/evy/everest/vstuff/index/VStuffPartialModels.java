package yay.evy.everest.vstuff.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

//@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VStuffPartialModels {

    public static final PartialModel WOODEN_PROPELLER_BLADE =
            PartialModel.of(VStuff.asResource("block/propeller/wooden_double_blade"));

//    @SubscribeEvent
//    public static void registerModels(ModelEvent.RegisterAdditional event) {
//        event.register(VStuff.asResource("block/propeller/wooden_double_blade"));
//        event.register(VStuff.asResource("block/rope_anchor/block"));
//    }

    public static PartialModel getWoodenDoubleBlade() {
        return WOODEN_PROPELLER_BLADE;
    }


    public static void init() {

    }
}