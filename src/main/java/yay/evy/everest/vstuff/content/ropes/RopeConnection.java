package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfig;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public class RopeConnection {

    public static class ConnectionInfo {

        String message = null;
        boolean valid = false;
        BlockPos blockPos0;
        BlockPos blockPos1;
        Vector3d worldPos0;
        Vector3d worldPos1;

        public ConnectionInfo withMessage(String message) {
            this.message = "rope." + message;
            return this;
        }
    }

    public static ConnectionInfo cached;

    static BlockPos hoveringPos;
    static boolean pullTaut;
    static ItemStack lastItem;

    static int extraTipWarmup;

    public static ConnectionInfo tryConnect(Level level, Player player, BlockPos blockPos1, BlockState hitState, String dimension, Long shipId1, ItemStack itemStack, boolean taut) {
        if (level.isClientSide && cached != null && blockPos1.equals(hoveringPos) && itemStack.equals(lastItem) && taut == pullTaut) return cached;

        ConnectionInfo info = new ConnectionInfo();
        hoveringPos = blockPos1;
        pullTaut = taut;
        lastItem = itemStack;
        cached = info;

        CompoundTag tag = itemStack.getTag();
        BlockPos blockPos0 = NbtUtils.readBlockPos(tag.getCompound("blockPos"));
        Vector3d worldPos0 = TagUtils.readVector3d(tag.getCompound("worldPos"));
        Vector3d worldPos1 = RopeUtils.getWorldPos(level, blockPos1, shipId1);

        String originDim = tag.getString("dim");


        if (level.isClientSide) {
            info.blockPos0 = blockPos0;
            info.blockPos1 = blockPos1;
            info.worldPos0 = worldPos0;
            info.worldPos1 = worldPos1;
        }

        if (originDim.equals(dimension))
            return info.withMessage("dimension");
        if (blockPos0.equals(blockPos1))
            return info.withMessage("click_to_reset");
        if (worldPos0.distance(worldPos1) > VStuffConfig.MAX_ROPE_LENGTH.get())
            return info.withMessage("too_long");

        info.valid = true;

        //todo: phys pulley and anchor checks

        return info;
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack stack = player.getMainHandItem();
        HitResult hitResult = Minecraft.getInstance().hitResult;
        int restoreWarmup = extraTipWarmup;
        extraTipWarmup = 0;

        if (hitResult == null) return;
        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        if (!VStuffItems.ROPE.is(stack)) { // check main hand then offhand, return if neither has rope item
            stack = player.getOffhandItem();
            if (!VStuffItems.ROPE.is(stack)) return;
        }

        if (!stack.hasFoil()) return;

        Level level = player.level();
        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState hitState = level.getBlockState(pos);
        Long shipId = ShipUtils.getLoadedShipIdAtPos(level, pos);
        String dimension = level.dimension().location().toString();

        extraTipWarmup = restoreWarmup;
        boolean taut = Minecraft.getInstance().options.keySprint.isDown();
        ConnectionInfo info = tryConnect(level, player, pos, hitState, dimension, shipId, stack, taut);

        if (extraTipWarmup < 20) extraTipWarmup++;

        if (!info.valid)
            extraTipWarmup = 0;
        if (info.valid)
            player.displayClientMessage(VStuff.translate("rope.can_connect")
                    .withStyle(ChatFormatting.GREEN), true);
        else if (info.message != null)
            player.displayClientMessage(VStuff.translate(info.message)
                    .withStyle(info.message.equals("rope.click_to_reset") ? ChatFormatting.WHITE : ChatFormatting.RED),
                    true);

    }

}
