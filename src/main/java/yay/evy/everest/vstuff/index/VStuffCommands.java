package yay.evy.everest.vstuff.index;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import yay.evy.everest.vstuff.infrastructure.commands.CreateRopeCommand;

public class VStuffCommands {

    public static void register(CommandDispatcher<CommandSourceStack> policeDispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> vstuffCommand = Commands.literal("vstuff");

        vstuffCommand.then(Commands.literal("test")
                        .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                        .then(Commands.argument("style", StringArgumentType.string())
                        .executes(CreateRopeCommand::createNewRope)))));
        /// todo create a RopeStylesArgument class for allowing this thing to make rope styles.


        policeDispatcher.register(vstuffCommand);

    }





}
