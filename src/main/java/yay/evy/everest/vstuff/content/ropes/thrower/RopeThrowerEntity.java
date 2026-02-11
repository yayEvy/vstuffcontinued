package yay.evy.everest.vstuff.content.ropes.thrower;

import kotlin.Pair;
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
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.ropes.pulley.PulleyAnchorBlockEntity;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import static yay.evy.everest.vstuff.internal.utility.ShipUtils.getShipIdAtPos;

public class RopeThrowerEntity extends ThrowableItemProjectile {


    public RopeThrowerEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);

    }

    private BlockPos startPos;
    private Long startShipId;
    private String startDimension;
    private RopeUtils.ConnectionType connectionType;
    private PhysPulleyBlockEntity waitingPulley;

    public void setStartData(
            BlockPos pos,
            Long shipId,
            String dimension,
            RopeUtils.ConnectionType type,
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

        if (connectionType == RopeUtils.ConnectionType.PULLEY && !(serverLevel.getBlockEntity(hitPos) instanceof PulleyAnchorBlockEntity)) {
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

        Pair<ReworkedRope, String> ropeResult = ReworkedRope.create(serverLevel, startShipId, secondShipId, startPos, hitPos, getOwner() instanceof Player p ? p : null, false);

        if (ropeResult.component1() != null) {

            if (connectionType == RopeUtils.ConnectionType.PULLEY
                    && waitingPulley != null
                    && serverLevel.getBlockEntity(hitPos) instanceof PulleyAnchorBlockEntity) {

                waitingPulley.attachRope(ropeResult.component1());
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
}