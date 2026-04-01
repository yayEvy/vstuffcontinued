package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff.LevituffAttachment;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.content.ships.reactionwheel.ReactionWheelAttachment;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.physgrabber.PhysGrabberServerAttachment;
import yay.evy.everest.vstuff.content.ships.thrust.ThrusterForceAttachment;
import yay.evy.everest.vstuff.index.*;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfig;
import yay.evy.everest.vstuff.internal.network.NetworkHandler;
import yay.evy.everest.vstuff.internal.network.PhysGrabberNetwork;
import org.valkyrienskies.core.api.VsBeta;

@Mod(VStuff.MOD_ID)
public class VStuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String NAME = "VStuff";

    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    @SuppressWarnings("removal") // sybau
    public VStuff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VStuffConfig.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, VStuffConfig.CLIENT_CONFIG);

        VStuffCreativeModeTabs.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);

        VStuffSounds.register(modEventBus);

        VStuffBlocks.register();
        VStuffItems.register();
        VStuffEntities.register(modEventBus);
        VStuffBlockEntities.register();
        //VStuffPackets.registerPackets();
        // todo fix these / make sure they work
        //VStuffConfigs.register(ModLoadingContext.get());

        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.registerPackets();
        PhysGrabberNetwork.register();

        modEventBus.addListener(this::commonSetup);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VStuffClient.initialize(modEventBus));

        LOGGER.info("{} ({}) initialized", VStuff.NAME, VStuff.MOD_ID);
    }

    @VsBeta
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(VStuff::registerAttachments);
    }

    public static void registerAttachments() {
        LOGGER.info("Registering {} attachments...", VStuff.MOD_ID);

        // thruster attachment ! yippee
        ValkyrienSkiesMod.getApi().registerAttachment(
                ThrusterForceAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );

        // phys grabber aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
        ValkyrienSkiesMod.getApi().registerAttachment(
                PhysGrabberServerAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );

        // robbing john propulsion :3dsmile:
        ValkyrienSkiesMod.getApi().registerAttachment(
                ReactionWheelAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );

        System.out.println("vstuff more like vs tuff :3dsmile:");
        ValkyrienSkiesMod.getApi().registerAttachment(
                LevituffAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );
    }

    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static MutableComponent translate(String key, Object... args) {
        Object[] args1 = LangBuilder.resolveBuilders(args);
        return Component.translatable(VStuff.MOD_ID + "." + key, args1);
    }
}
