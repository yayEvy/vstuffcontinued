package yay.evy.everest.vstuff.magnetism;

import net.minecraft.server.level.ServerLevel;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ShipMagnetismHandler {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            event.getServer().getAllLevels().forEach(level -> {
                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    VSGameUtilsKt.getAllShips(serverLevel).forEach(ship -> {
                        if (ship instanceof ServerShip) {
                            ServerShip serverShip = (ServerShip) ship;
                            MagnetismAttachment.getOrCreate(serverShip, serverLevel);
                        }
                    });
                }
            });
        }
    }
}