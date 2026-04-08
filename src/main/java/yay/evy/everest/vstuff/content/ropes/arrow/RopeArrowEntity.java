package yay.evy.everest.vstuff.content.ropes.arrow;//package yay.evy.everest.vstuff.content.ropes.arrow;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;

public class RopeArrowEntity extends AbstractArrow {



    public RopeArrowEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public RopeArrowEntity(EntityType<? extends AbstractArrow> pEntityType, double pX, double pY, double pZ, Level pLevel) {
        super(pEntityType, pX, pY, pZ, pLevel);
    }

    public RopeArrowEntity(EntityType<? extends AbstractArrow> pEntityType, LivingEntity pShooter, Level pLevel) {
        super(pEntityType, pShooter, pLevel);
    }




    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(VStuffItems.ROPE_ARROW);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos secondPos = result.getBlockPos();
        Entity entity = this.getOwner();
        ItemStack itemStack = this.getPickupItem();

        if (entity instanceof Player player){
        if (RopeArrowItem.getClickedPos() != null) {
        if (this.level() instanceof ServerLevel serverLevel) {

            BlockPos firstPos = RopeArrowItem.getClickedPos();

            Long firstShipId = getShipIdAtPos(serverLevel, firstPos);
            Long secondShipId = getShipIdAtPos(serverLevel, secondPos);

            RopeFactory.createNewRope(serverLevel,firstShipId,secondShipId,firstPos,secondPos,
                    RopeStyle.getOrDefaultStyleId(itemStack.getOrCreateTag()), player);




          //  System.out.println("FIRST: " + firstPos);
        //    System.out.println("SECOND: " + secondPos);
        }
        }
        if(RopeArrowItem.getClickedPos() == null){
                sendRopeMessage(player, "no_first_pos");
            }
        }

       // discard();

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return super.getCapability(cap);
    }

    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    private void sendRopeMessage(Player player, String name) {
        player.displayClientMessage(
                Component.translatable("vstuff.message." + name),
                true
        );
    }

}

