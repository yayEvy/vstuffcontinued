package yay.evy.everest.vstuff.infrastructure.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.RopeManager;

import java.util.Objects;

public class DeleteRopeCommand {

    public static int deleteRope (CommandContext<CommandSourceStack> csx) {
        int idToDelete = IntegerArgumentType.getInteger(csx, "RopeID");
         RopeManager level = RopeManager.get(csx.getSource().getLevel());

        if(level.hasRope(idToDelete)){
            Objects.requireNonNull(csx.getSource().getPlayer()).displayClientMessage(Component.literal("Deleted Rope with ID: " + idToDelete), false);
            RopeFactory.removeRope(csx.getSource().getLevel(), idToDelete);
        } else {Objects.requireNonNull(csx.getSource().getPlayer()).displayClientMessage(Component.literal("No Such Rope Exists"), false);}

        return 1;
    }

}