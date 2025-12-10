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
        if (!this.level().isClientSide()) {                                    // Checks For Server
            this.level().broadcastEntityEvent(this, ((byte) 3));         //IDFK
            if (this.level() instanceof ServerLevel serverLevel) {             // I just wanted the server level :sob:
            Entity entity = this.getOwner();                                   // gets the player from entity  (slavery? i thought we abolished that)
            if (entity instanceof Player player) {                             // ^^^

                BlockPos firstPos = result.getBlockPos();                        // all of this is for the rope making, mostly stuff i stole from the rope item (I am evil muahahahahahahhahah)
                BlockPos secondPos = player.getOnPos();
                Long firstShipId = getShipIdAtPos(serverLevel, firstPos);
                Long secondShipId = getShipIdAtPos(serverLevel, secondPos);

            if(!level().getBlockState(secondPos).isAir()) {                          // checks to make sure you arent flying because it looks wrong and evil when you dont check for it
                RopeUtil.RopeReturn ropeReturn = Rope.createNew(VStuffItems.LEAD_CONSTRAINT_ITEM.get(), serverLevel, // creates the rope, the rope constraint really just pulls from the LeadConstraintItem class because why reinvent the wheel amiright? ( I will see myself out)
                        firstPos, secondPos, firstShipId, secondShipId, player);
            }

                this.discard();  // Discord??????????
            }}
            super.onHitBlock(result);
        }

    }


    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

}