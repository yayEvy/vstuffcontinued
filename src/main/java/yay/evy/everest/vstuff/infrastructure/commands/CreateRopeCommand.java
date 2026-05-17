package yay.evy.everest.vstuff.infrastructure.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

import java.util.Objects;

public class CreateRopeCommand {

    @SuppressWarnings({"SameReturnValue", "IfStatementWithIdenticalBranches"})
    public static int createNewRope(CommandContext<CommandSourceStack> csx) throws CommandSyntaxException {

        ServerLevel level = csx.getSource().getLevel();
        Coordinates coordsFrom = csx.getArgument("from", Coordinates.class);
        Coordinates coordsTo = csx.getArgument("to", Coordinates.class);
        BlockPos from = BlockPosArgument.getLoadedBlockPos(csx, "from");
        BlockPos to = BlockPosArgument.getLoadedBlockPos(csx, "to");
        Long shipOne = ShipUtils.getLoadedShipIdAtPos(level, from);
        Long shipTwo = ShipUtils.getLoadedShipIdAtPos(level, to);
        ResourceLocation location = RopeStyleManager.returnOrFallback(ResourceLocation.parse(StringArgumentType.getString(csx,"style")));
        Entity toSuffer = csx.getSource().getEntity();

        if(coordsTo.isYRelative()){ to = new BlockPos(to.getX(),to.getY()-1,to.getZ());}
        if(coordsFrom.isYRelative()){ from = new BlockPos(from.getX(),from.getY()-1,from.getZ());}

        Block blocky = level.getBlockState(from).getBlock();
        Block rocky = level.getBlockState(to).getBlock();



        if (blocky.toString().equals("Block{minecraft:air}") || rocky.toString().equals("Block{minecraft:air}")) {
            Objects.requireNonNull(csx.getSource().getPlayer()).displayClientMessage(VStuff.translate("vstuff.message.pos_is_air").withStyle(ChatFormatting.RED), true);
            return 1;
        } else {
        ReworkedRope rope = RopeFactory.createNewRope(level,shipOne,shipTwo,from,to,location,toSuffer);

        if(RopeFactory.RopeResult.validResult(rope).valid) {
            Objects.requireNonNull(csx.getSource().getPlayer()).displayClientMessage(VStuff.translate("vstuff.message.rope.created").withStyle(ChatFormatting.GREEN), true);
        } else Objects.requireNonNull(csx.getSource().getPlayer()).displayClientMessage(VStuff.translate("vstuff.message.general_error").withStyle(ChatFormatting.YELLOW), true);

        return 1;
        }

    }}
