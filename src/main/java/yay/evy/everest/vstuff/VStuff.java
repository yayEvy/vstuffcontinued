package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import yay.evy.everest.vstuff.index.*;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.particles.ParticleTypes;

@Mod(VStuff.MOD_ID)
public class VStuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public VStuff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        VStuffCreativeModeTabs.register(modEventBus);
        VStuffShapes.register();

        VStuffBlockEntities.register();
        VStuffBlocks.register();
        VStuffItems.register();

        VStuffSounds.register(modEventBus);

        ParticleTypes.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        NetworkHandler.registerPackets();

        LOGGER.info("VStuff mod initialized");
    }

    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VstuffConfig.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, VstuffConfig.CLIENT_CONFIG);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.sendClearAllConstraintsToPlayer(player);
        }
    }
}
