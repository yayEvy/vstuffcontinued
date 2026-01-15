package yay.evy.everest.vstuff.content.rope.ropethrower;

import kotlin.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.rope.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.rope.roperework.Rope;
import yay.evy.everest.vstuff.content.rope.roperework.RopeUtil;
import yay.evy.everest.vstuff.foundation.utility.PosUtils;
import yay.evy.everest.vstuff.index.VStuffItems;

public class RopeThrowerEntity extends ThrowableItemProjectile {

    public RopeThrowerEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);

    }

    private BlockPos startPos;
    private Long startShipId;
    private String startDimension;
    private RopeUtil.SelectType selectType;

    public void setStartData(
            BlockPos pos,
            Long shipId,
            String dimension,
            RopeUtil.SelectType type
    ) {
        this.startPos = pos;
        this.startShipId = shipId;
        this.startDimension = dimension;
        this.selectType = type;
    }

    @Override
    protected Item getDefaultItem() {
        return VStuffItems.ROPE_THROWER_ITEM.get();
    }


    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Player owner = null;
        if (getOwner() instanceof Player player) {
            owner = player;
        }

        if (!serverLevel.dimension().location().toString().equals(startDimension)) {
            if (owner != null) owner.displayClientMessage(VStuff.translate("interdimensional").withStyle(ChatFormatting.RED), true);
            discard();
            return;
        }

        BlockPos hitPos = hitResult.getBlockPos().immutable();
        Long secondShipId = PosUtils.getLoadedShipIdAtPos(serverLevel, hitPos);

        if (!PosUtils.isCompatibleWithType(serverLevel, hitPos, selectType)) {
            ItemStack ropeDrop = new ItemStack(VStuffItems.ROPE_ITEM.get());
            ItemEntity itemEntity = new ItemEntity(
                    serverLevel,
                    hitPos.getX() + 0.5,
                    hitPos.getY() + 0.5,
                    hitPos.getZ() + 0.5,
                    ropeDrop
            );
            serverLevel.addFreshEntity(itemEntity);

            discard();
            return;
        }

        Pair<Rope, String> result = Rope.create(serverLevel, startShipId, secondShipId, startPos, hitPos, owner, false);

        if (result.component1() == null) {
            if (serverLevel.getBlockEntity(startPos) instanceof PhysPulleyBlockEntity pulleyBE) {
                pulleyBE.resetSelf();
            }
        }

        if (selectType == RopeUtil.SelectType.PULLEY && serverLevel.getBlockEntity(startPos) instanceof PhysPulleyBlockEntity pulleyBE) {
            pulleyBE.attachRope(result.component1());
        }

        serverLevel.playSound(
                null,
                hitPos,
                net.minecraft.sounds.SoundEvents.LEASH_KNOT_PLACE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        discard();
    }
}