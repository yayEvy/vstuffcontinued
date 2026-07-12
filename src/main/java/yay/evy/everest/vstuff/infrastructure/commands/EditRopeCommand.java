package yay.evy.everest.vstuff.infrastructure.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class EditRopeCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register(){
        return Commands.literal("edit")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("Not implemented"), false);
                    // command of doom and despair
                    return 1;
                });
    }

}
