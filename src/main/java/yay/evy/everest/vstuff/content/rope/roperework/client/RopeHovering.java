package yay.evy.everest.vstuff.content.rope.roperework.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.VStuffConfig;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.client.ClientRopeUtil;
import yay.evy.everest.vstuff.content.rope.roperework.RopeUtil;
import yay.evy.everest.vstuff.foundation.RopeStyles;
import yay.evy.everest.vstuff.foundation.utility.PosUtils;
import yay.evy.everest.vstuff.index.VStuffItems;

public class RopeHovering {

    public static RopeInfo cached;

    static BlockPos hoveringPos;
    static boolean hoveringTaut;
    static ItemStack lastItem;

    static int extraTipWarmup;

    public static class RopeInfo {

        boolean valid = false;
        boolean pulley = false;
        public String message = null;
        Vector3f localPos0;
        Vector3f localPos1;
        Vector3f worldPos0;
        Vector3f worldPos1;
        Long ship0;
        Long ship1;
        float length;
        RopeStyles.RopeStyle style;

        public static RopeInfo tryConnect(Level level, Player player, BlockPos pos, Long ship, String dimId, BlockState hitState, ItemStack stack, boolean taut) {
            float maxLength = VStuffConfig.MAX_ROPE_LENGTH.get();

            if (level.isClientSide() && cached != null && pos.equals(hoveringPos) && stack.equals(lastItem) && hoveringTaut == taut) {
                return cached; // has not changed
            }

            RopeInfo info = new RopeInfo();

            hoveringTaut = taut;
            hoveringPos = pos;
            lastItem = stack;
            cached = info;

            CompoundTag tag = stack.getTag().getCompound("first");

            BlockPos blockPos0 = NbtUtils.readBlockPos(tag.getCompound("pos"));
            Long ship0 = tag.getLong("shipId");
            String dimId0 = tag.getString("dim");

            if (level.isClientSide) {
                info.localPos0 = RopeUtil.getRopeConnectionPos(level, blockPos0);
                info.worldPos0 = ClientRopeUtil.renderLocalToWorld(level, info.localPos0, ship0);
                info.ship0 = ship0;
                info.localPos1 = RopeUtil.getRopeConnectionPos(level, pos);
                info.worldPos1 = ClientRopeUtil.renderLocalToWorld(level, info.localPos1, ship);
                info.ship1 = ship;
            }

            float length = info.worldPos0.distance(info.worldPos1);

            if (level.isEmptyBlock(blockPos0)) return info.withMessage("missing_first");
            if (!dimId0.equals(dimId)) return info.withMessage("interdimensional");
            if (info.worldPos0.equals(info.worldPos1)) return info.withMessage("second_point");
            if (length > maxLength) return info.withMessage("too_long");
            if (PosUtils.isPhysPulley(hitState)) return info.withMessage("invalid_second");
            if (PosUtils.isPulleyAnchor(hitState) && PosUtils.isPhysPulley(level, pos)) {
                info.pulley = true;
            } else if (PosUtils.isPulleyAnchor(hitState)) {
                return info.withMessage("invalid_second");
            }

            info.valid = true;
            ClientRopeManager.setPreviewRope(info.ship0, info.ship1, info.localPos0, info.localPos1, info.length, info.style);
            return info;
        }

        public RopeInfo withMessage(String message) {
            this.message = "rope." + message;
            return this;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack stack = player.getMainHandItem();
        HitResult hitResult = Minecraft.getInstance().hitResult;
        int restoreWarmup = extraTipWarmup;
        extraTipWarmup = 0;

        if (hitResult == null) {
            if (ClientRopeManager.hasPreviewRope()) ClientRopeManager.clearPreviewRope();
            return;
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            if (ClientRopeManager.hasPreviewRope()) ClientRopeManager.clearPreviewRope();
            return;
        }
        if (!stack.getItem().equals(VStuffItems.ROPE_ITEM.get())) {
            if (ClientRopeManager.hasPreviewRope()) ClientRopeManager.clearPreviewRope();
            return;
        }

        if (!stack.hasFoil() ) {
            if (ClientRopeManager.hasPreviewRope()) ClientRopeManager.clearPreviewRope();
            return;
        }

        Level level = player.level();
        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);
        Long ship = PosUtils.getShipIdAtPos(level, pos);
        String dimId = level.dimension().location().toString();

        extraTipWarmup = restoreWarmup;
        boolean taut = Minecraft.getInstance().options.keySprint.isDown();
        RopeInfo info = RopeInfo.tryConnect(level, player, pos, ship, dimId, hitState, stack, taut);
        if (extraTipWarmup < 20)
            extraTipWarmup++;
        if (!info.valid || !hoveringTaut)
            extraTipWarmup = 0;

        if (info.valid)
            player.displayClientMessage(VStuff.translate("rope.can_connect")
                    .withStyle(ChatFormatting.GREEN), true);
        else if (info.message != null)
            player.displayClientMessage(VStuff.translate(info.message)
                            .withStyle(info.message.equals("rope.second_point") ? ChatFormatting.WHITE : ChatFormatting.RED),
                    true);
    }
}
