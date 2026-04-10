package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff.LevituffAttachment;
import yay.evy.everest.vstuff.content.ropes.arrow.RopeArrowRenderer;
import yay.evy.everest.vstuff.content.ships.reactionwheel.ReactionWheelAttachment;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.physgrabber.PhysGrabberServerAttachment;
import yay.evy.everest.vstuff.content.ships.thrust.ThrusterForceAttachment;
import yay.evy.everest.vstuff.index.*;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;
import org.valkyrienskies.core.api.VsBeta;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.infrastructure.data.VStuffDatagen;


@Mod(VStuff.MOD_ID)
public class VStuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String NAME = "VStuff";

    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID); //todo implement tooltip modifier factory like create's

    public VStuff(FMLJavaModLoadingContext modLoadingContext) {
        IEventBus modEventBus = modLoadingContext.getModEventBus();

        VStuffRegistries.init();

        VStuffCreativeModeTabs.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);

        VStuffBlocks.register();
        VStuffBlockEntities.register();
        VStuffConfigs.register(modLoadingContext);
        VStuffEntities.register();
        VStuffItems.register();
        VStuffPackets.register();
        VStuffSounds.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(VStuff::commonSetup);
        modEventBus.addListener(EventPriority.LOWEST, VStuffDatagen::gatherData);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VStuffClient.initialize(modEventBus));

        LOGGER.info("{} ({}) initialized", VStuff.NAME, VStuff.MOD_ID);
    }

    @VsBeta
    private static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(VStuff::registerAttachments);
    }

    public static void registerAttachments() {
        registerAttachment(ThrusterForceAttachment.class);
        registerAttachment(PhysGrabberServerAttachment.class);
        registerAttachment(ReactionWheelAttachment.class);
        System.out.println("vstuff more like vs tuff");
        registerAttachment(LevituffAttachment.class);
    }

    private static <A extends ShipPhysicsListener> void registerAttachment(Class<A> attachment) {
        ValkyrienSkiesMod.getApi().registerAttachment(attachment, attachmentBuilder -> {
            attachmentBuilder.build();
            return null;
        });
    }



    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static ResourceLocation mcResource(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    public static MutableComponent translate(String key, Object... args) {
        Object[] args1 = LangBuilder.resolveBuilders(args);
        return Component.translatable(VStuff.MOD_ID + "." + key, args1);
    }
}
