package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import yay.evy.everest.vstuff.blocks.ModBlockEntities;
import yay.evy.everest.vstuff.blocks.ModBlocks;
import yay.evy.everest.vstuff.item.ModCreativeModTabs;
import yay.evy.everest.vstuff.item.ModItems;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.sound.ModSounds;

@Mod(vstuff.MOD_ID)
public class vstuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public vstuff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        ModBlocks.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        ModCreativeModTabs.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());



        REGISTRATE.registerEventListeners(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        NetworkHandler.registerPackets();

        LOGGER.info("VStuff mod initialized");
    }

    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.sendClearAllConstraintsToPlayer(player);
        }
    }
}
