package yay.evy.everest.vstuff.content.ropethrower;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.constraint.Rope;
import yay.evy.everest.vstuff.content.constraint.RopeUtil;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffItems;

public class RopeThrowerEntity extends ThrowableItemProjectile {

    private BlockPos ownerBlockPos;

    public void setOwnerBlockPos(BlockPos pos) {
        this.ownerBlockPos = pos;
    }
    private boolean isDispenserShot = false;

    public void setDispenserShot(boolean value) {
        this.isDispenserShot = value;
    }

    public RopeThrowerEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);

    }

    public RopeThrowerEntity(Level level) {
        super(VStuffEntities.ROPE_THROWER.get(), level);
    }

    public RopeThrowerEntity(Level level, LivingEntity livingEntity) {
        super(VStuffEntities.ROPE_THROWER.get(), livingEntity, level);
    }

    @Override
    protected Item getDefaultItem() {
        return VStuffItems.ROPE_THROWER_ITEM.get();
    }



    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) { // Checks For Server
            this.level().broadcastEntityEvent(this, ((byte) 3));   //IDFK
            if (this.level() instanceof ServerLevel serverLevel) {  // I just wanted the server level :sob:
                Entity entity = this.getOwner();      // gets the player from entity  (slavery? i thought we abolished that)

                BlockPos firstPos = result.getBlockPos();
                BlockPos secondPos;

                if (entity instanceof Player player) {  // ^^^
                    if (isDispenserShot && ownerBlockPos != null) {   // all of this is for the rope making, mostly stuff i stole from the rope item (I am evil muahahahahahahhahah)
                        secondPos = ownerBlockPos;
                    } else if (player instanceof net.minecraftforge.common.util.FakePlayer) {
                        secondPos = player.blockPosition();
                    } else {
                        secondPos = player.getOnPos();
                    }

                    if (isDispenserShot || !level().getBlockState(secondPos).isAir()) { // checks to make sure you arent flying because it looks wrong and evil when you dont check for it
                        Long firstShipId = getShipIdAtPos(serverLevel, firstPos);
                        Long secondShipId = getShipIdAtPos(serverLevel, secondPos);

                        RopeUtil.RopeReturn ropeReturn = Rope.createNew(  // creates the rope, the rope constraint really just pulls from the LeadConstraintItem class because why reinvent the wheel amiright? ( I will see myself out)
                                VStuffItems.LEAD_CONSTRAINT_ITEM.get(),
                                serverLevel,
                                firstPos,
                                secondPos,
                                firstShipId,
                                secondShipId,
                                player
                        );
                    }
                }
                this.discard();  // Discord??????????
            }
            super.onHitBlock(result);
        }
    }


    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }
}