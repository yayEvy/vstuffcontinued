package yay.evy.everest.vstuff.index;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import yay.evy.everest.vstuff.infrastructure.commands.CreateRopeCommand;
import yay.evy.everest.vstuff.infrastructure.commands.DeleteAllRopesCommand;
import yay.evy.everest.vstuff.infrastructure.commands.DeleteRopeCommand;
import yay.evy.everest.vstuff.infrastructure.commands.RerenderAllRopesCommand;

public class VStuffCommands {

    public static void register(CommandDispatcher<CommandSourceStack> policeDispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> vstuffCommand = Commands.literal("vstuff");

        vstuffCommand.then(Commands.literal("CreateNewRope")
                        .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                        .then(Commands.argument("style", StringArgumentType.string())
                                .requires( commandSourceStack -> commandSourceStack.hasPermission(2))
                        .executes(CreateRopeCommand::createNewRope)))));
        /// todo create a RopeStylesArgument class for allowing this thing to make rope styles.

        vstuffCommand.then(Commands.literal("DeleteAllRopes")
                .requires( commandSourceStack -> commandSourceStack.hasPermission(2))
                        .executes(DeleteAllRopesCommand::deleteCantaRopes));

        vstuffCommand.then(Commands.literal("DeleteRope")
                        .then(Commands.argument("RopeID", IntegerArgumentType.integer())
                                .requires( commandSourceStack -> commandSourceStack.hasPermission(2))
                        .executes(DeleteRopeCommand::deleteRope)));



        policeDispatcher.register(vstuffCommand);

    }





}
