package yay.evy.everest.vstuff.infrastructure.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class VStuffCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("vstuff")
                .requires(ctx -> ctx.hasPermission(3))
                        .then(CreateRopeCommand.register())
                        .then(DeleteRopesCommand.remove())
                        .then(DeleteRopesCommand.removeAll())
                        .then(FindRopeCommand.register())
                ;

        dispatcher.register(root);

    }
}
