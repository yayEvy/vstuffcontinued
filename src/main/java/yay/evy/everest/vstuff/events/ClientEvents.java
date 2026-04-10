package yay.evy.everest.vstuff.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.styler.RopeStyleMenuHandler;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.internal.styling.RopeRestyleManager;

import static yay.evy.everest.vstuff.internal.utility.RopeUtils.findTargetedLeadClient;

@EventBusSubscriber(modid = VStuff.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            switch (event.phase){
                case START -> onClientTickStart(Minecraft.getInstance());
                case END -> onClientTickEnd(Minecraft.getInstance());
            }
        }

        public static void onClientTickStart(Minecraft mc) {
            if (isGameActive()) {
                RopeStyleMenuHandler.clientTick();
            }
        }

        public static void onClientTickEnd(Minecraft mc) {}

        protected static boolean isGameActive() {
            return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (Minecraft.getInstance().screen != null)
                return;
            int key = event.getKey();
            boolean pressed = event.getAction() != 0;
            RopeStyleMenuHandler.onKeyInput(key, pressed);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) { // fired when right-clicking air // i see
            handleRightClickEvent(event);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) { // fired when right-clicking a block // oh ok
            handleRightClickEvent(event);
        }

        private static void handleRightClickEvent(PlayerInteractEvent event) {
            ItemStack itemStack = event.getItemStack();
            if (RopeRestyleManager.isValidRetyping(itemStack.getItem())) {
                if ((event.getLevel() instanceof ClientLevel level)) {
                    if (findTargetedLeadClient(level, event.getEntity()) != null) {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                    }
                }
            }
        }
}
