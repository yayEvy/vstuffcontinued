package yay.evy.everest.vstuff.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.sound.RopeSoundHandler;
import yay.evy.everest.vstuff.VStuff;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RopeSoundCommand {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("ropesounds")
                .executes(RopeSoundCommand::toggleRopeSounds));
    }

    private static int toggleRopeSounds(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        RopeSoundHandler.toggle();
        boolean enabled = RopeSoundHandler.isEnabled();

        Minecraft.getInstance().player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("Rope sounds " + (enabled ? "enabled" : "disabled")),
                true
        );
        return 1;
    }
}
