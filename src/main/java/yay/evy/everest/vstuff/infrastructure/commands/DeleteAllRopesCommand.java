package yay.evy.everest.vstuff.infrastructure.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import java.util.Objects;

public class DeleteAllRopesCommand {

    public static int deleteCantaRopes(CommandContext<CommandSourceStack> csx) {

        RopeManager manager = RopeManager.get(csx.getSource().getLevel());

        String sOrNoS = manager.getIdList().size() <= 1 ? " " : "s";

        if (!manager.getIdList().isEmpty()) {
            Objects.requireNonNull(csx.getSource().getPlayer()).displayClientMessage(Component.literal("Deleted " + manager.getIdList().size() + " Rope" + sOrNoS), false);
        } else { Objects.requireNonNull(csx.getSource().getPlayer()).displayClientMessage(Component.literal("Deleted 0 Ropes"), false);}

        for (int evil : manager.getIdList()) {
            RopeFactory.removeRope(csx.getSource().getLevel(), evil);
        }


        return 1;
    }
}
