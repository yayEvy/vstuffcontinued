package yay.evy.everest.vstuff.content.ropethrower;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.constraint.Rope;
import yay.evy.everest.vstuff.content.constraint.RopeUtil;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.pulley.PulleyAnchorBlockEntity;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffItems;

public class RopeThrowerEntity extends ThrowableItemProjectile {


    public RopeThrowerEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);

    }

    private BlockPos startPos;
    private Long startShipId;
    private String startDimension;
    private RopeUtil.ConnectionType connectionType;
    private PhysPulleyBlockEntity waitingPulley;

    public void setStartData(
            BlockPos pos,
            Long shipId,
            String dimension,
            RopeUtil.ConnectionType type,
            PhysPulleyBlockEntity pulley
    ) {
        this.startPos = pos;
        this.startShipId = shipId;
        this.startDimension = dimension;
        this.connectionType = type;
        this.waitingPulley = pulley;
    }

    @Override
    protected Item getDefaultItem() {
        return VStuffItems.ROPE_THROWER_ITEM.get();
    }


    @Override
    protected void onHitBlock(BlockHitResult result) {
       // System.out.println("on hit");
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (startPos == null || !serverLevel.dimension().location().toString().equals(startDimension)) {
            discard();
            return;
        }

        BlockPos hitPos = result.getBlockPos().immutable();
        Long secondShipId = getShipIdAtPos(serverLevel, hitPos);

        if (connectionType == RopeUtil.ConnectionType.PULLEY && !(serverLevel.getBlockEntity(hitPos) instanceof PulleyAnchorBlockEntity)) {
            ItemStack ropeDrop = new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get());
            ItemEntity itemEntity = new ItemEntity(
                    serverLevel,
                    hitPos.getX() + 0.5,
                    hitPos.getY() + 1.5,
                    hitPos.getZ() + 0.5,
                    ropeDrop
            );
            serverLevel.addFreshEntity(itemEntity);

            discard();
            return;
        }

        RopeUtil.RopeReturn ropeReturn = Rope.createNew(
                VStuffItems.LEAD_CONSTRAINT_ITEM.get(),
                serverLevel,
                startPos,
                hitPos,
                startShipId,
                secondShipId,
                getOwner() instanceof Player p ? p : null
        );

        if (ropeReturn.result() == RopeUtil.RopeInteractionReturn.SUCCESS) {

            if (connectionType == RopeUtil.ConnectionType.PULLEY
                    && waitingPulley != null
                    && serverLevel.getBlockEntity(hitPos) instanceof PulleyAnchorBlockEntity) {

                waitingPulley.attachRope(ropeReturn.rope());
            }


            serverLevel.playSound(
                    null,
                    hitPos,
                    net.minecraft.sounds.SoundEvents.LEASH_KNOT_PLACE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        discard();
    }



    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }
}