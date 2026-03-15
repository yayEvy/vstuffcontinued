package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
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
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfig;
import yay.evy.everest.vstuff.internal.RopeStyleManager;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public class RopeConnection {

    public static class ConnectionInfo {

        String message = null;
        boolean valid = false;
        BlockPos blockPos0;
        BlockPos blockPos1;
        Vector3d localPos0;
        Vector3d localPos1;
        Long ship0;
        Long ship1;
        double length;
        ResourceLocation style;

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

        double length = worldPos0.distance(worldPos1) + (taut ? 0.0 : 0.5);

        if (level.isClientSide) {
            info.blockPos0 = blockPos0;
            info.blockPos1 = blockPos1;
            info.localPos0 = RopeUtils.getLocalPos(level, blockPos0);
            info.localPos1 = RopeUtils.getLocalPos(level, blockPos1);
            info.ship0 = tag.getLong("shipId");
            info.ship1 = ShipUtils.getLoadedShipIdAtPos(level, blockPos1);
            info.length = length;
            info.style = RopeStyleManager.getStyle(player);
        }

        if (originDim.equals(dimension))
            return info.withMessage("dimension");
        if (blockPos0.equals(blockPos1))
            return info.withMessage("click_to_reset");
        if (length > VStuffConfig.MAX_ROPE_LENGTH.get())
            return info.withMessage("too_long");
        if (!IRopeActor.canActorAttach(hitState))
            return info.withMessage("actor_connected");

        info.valid = true;

        return info;
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack stack = player.getMainHandItem();
        HitResult hitResult = Minecraft.getInstance().hitResult;
        int restoreWarmup = extraTipWarmup;
        extraTipWarmup = 0;

        if (hitResult == null) {
            clearRope();
            return;
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            clearRope();
            return;
        }

        if (!VStuffItems.ROPE.is(stack)) { // check main hand then offhand, return if neither has rope item
            stack = player.getOffhandItem();
            if (!VStuffItems.ROPE.is(stack)) {
                clearRope();
                return;
            }
        }

        if (!stack.hasFoil()) {
            clearRope();
            return;
        }

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

        if (!info.valid) {
            extraTipWarmup = 0;
            clearRope();
        }
        if (info.valid) {
            player.displayClientMessage(VStuff.translate("rope.can_connect")
                    .withStyle(ChatFormatting.GREEN), true);

            if (info != cached) ClientRopeManager.setPreviewRope(
                    info.ship0,
                    info.ship1,
                    info.localPos0,
                    info.localPos1,
                    (float) info.length,
                    info.style
            );
        }
        else if (info.message != null)
            player.displayClientMessage(VStuff.translate(info.message)
                    .withStyle(info.message.equals("rope.click_to_reset") ? ChatFormatting.WHITE : ChatFormatting.RED),
                    true);

    }

    @OnlyIn(Dist.CLIENT)
    public static void clearRope() {
        if (ClientRopeManager.hasPreviewRope()) ClientRopeManager.clearPreviewRope();
    }

}
