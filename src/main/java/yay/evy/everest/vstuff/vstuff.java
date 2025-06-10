package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import yay.evy.everest.vstuff.block.ModBlockEntities;
import yay.evy.everest.vstuff.block.ModBlocks;
import yay.evy.everest.vstuff.events.WorldEventHandler;
import yay.evy.everest.vstuff.item.ModCreativeModTabs;
import yay.evy.everest.vstuff.item.ModItems;
import yay.evy.everest.vstuff.magnetism.MagnetismTickHandler;
import yay.evy.everest.vstuff.magnetism.ShipMagnetismHandler;
import yay.evy.everest.vstuff.network.NetworkHandler;
import yay.evy.everest.vstuff.ropes.ConstraintPersistence;
import yay.evy.everest.vstuff.ropes.RopePulleyRenderer;

@Mod(vstuff.MOD_ID)
public class vstuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();

    public vstuff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register in correct order
        ModBlocks.register(modEventBus);           // Blocks first
        ModBlockEntities.register(modEventBus);    // Block entities second
        ModItems.register(modEventBus);            // Items third
        ModCreativeModTabs.register(modEventBus);  // Creative tabs last

        // Register event handlers to Forge event bus
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(MagnetismTickHandler.class);
        MinecraftForge.EVENT_BUS.register(ShipMagnetismHandler.class);
        MinecraftForge.EVENT_BUS.register(WorldEventHandler.class);

        //BlockEntityRenderers.register(ModBlockEntities.ROPE_PULLEY.get(), RopePulleyRenderer::new);


        NetworkHandler.registerPackets();
        LOGGER.info("VStuff mod initialized");


    }


}
