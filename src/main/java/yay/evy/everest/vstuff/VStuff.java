package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
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
import org.valkyrienskies.core.api.attachment.AttachmentRegistration;
import org.valkyrienskies.mod.api.VsApi;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.client.VStuffClient;
import yay.evy.everest.vstuff.content.physgrabber.PhysGrabberServerAttachment;
import yay.evy.everest.vstuff.content.thrust.ThrusterForceAttachment;
import yay.evy.everest.vstuff.events.ColorHaggler;
import yay.evy.everest.vstuff.index.*;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.network.PhysGrabberNetwork;
import yay.evy.everest.vstuff.particles.ParticleTypes;
import org.valkyrienskies.core.api.VsBeta;
import kotlin.Unit;

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
        PhysGrabberNetwork.register();
        MinecraftForge.EVENT_BUS.register(new ColorHaggler());

        modEventBus.addListener(this::commonSetup);


        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> VStuffClient::initialize);


        LOGGER.info("VStuff mod initialized");
    }
    @VsBeta
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerAttachments();
        });
    }
    public static void registerAttachments() {
        LOGGER.info("Registering vstuff attachments...");

        ValkyrienSkiesMod.getApi().registerAttachment(
                ThrusterForceAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );


        ValkyrienSkiesMod.getApi().registerAttachment(
                PhysGrabberServerAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );


        // Add other attachments when needed :3
    }
    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static ResourceLocation asTextureResource(String path) {
        return new ResourceLocation(MOD_ID, "textures/" + path);
    }

    public static ResourceLocation asModelResource(String path) {
        return new ResourceLocation(MOD_ID, "models/" + path);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.sendClearAllConstraintsToPlayer(player);
        }
    }



}


