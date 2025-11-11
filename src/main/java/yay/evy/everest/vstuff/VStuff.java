package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist; // Import the Dist class
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor; // Import the DistExecutor class
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import yay.evy.everest.vstuff.client.VStuffClient;
import yay.evy.everest.vstuff.index.*;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.particles.ParticleTypes;

@Mod(VStuff.MOD_ID)
public class VStuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String NAME = "VStuff";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public VStuff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VstuffConfig.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, VstuffConfig.CLIENT_CONFIG);

        VStuffCreativeModeTabs.register(modEventBus);
        VStuffSounds.register(modEventBus);
        ParticleTypes.register(modEventBus);
        REGISTRATE.registerEventListeners(modEventBus);

        VStuffRopeStyles.register();
        VStuffBlocks.register();
        VStuffBlockEntities.register();
        VStuffItems.register();

        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.registerPackets();


        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> VStuffClient::initialize);


        LOGGER.info("VStuff mod initialized");
    }


    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    public static ResourceLocation getRopeStyle(String style) {
        return new ResourceLocation(MOD_ID, "textures/entity/rope/rope_" + style + ".png");
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.sendClearAllConstraintsToPlayer(player);
        }
    }
}


