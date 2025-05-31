package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import yay.evy.everest.vstuff.block.ModBlocks;
import yay.evy.everest.vstuff.item.ModCreativeModTabs;
import yay.evy.everest.vstuff.item.ModItems;
import yay.evy.everest.vstuff.network.NetworkHandler;


@Mod(vstuff.MOD_ID)
public class vstuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();

    public vstuff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        NetworkHandler.registerPackets();

        LOGGER.info("VStuff mod initialized");


    }


}
