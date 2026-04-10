package yay.evy.everest.vstuff.content.ropes.arrow;//package yay.evy.everest.vstuff.content.ropes.arrow;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

import static yay.evy.everest.vstuff.internal.utility.ShipUtils.getShipIdAtPos;

public class RopeArrowEntity extends AbstractArrow {

    private ResourceLocation styleId;
    private BlockPos firstPos;
    private String firstDim;

    public RopeArrowEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public RopeArrowEntity(EntityType<? extends AbstractArrow> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }


    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos secondPos = result.getBlockPos();
        Entity entity = this.getOwner();

        if (firstPos != null) {
            if (!(this.level() instanceof ServerLevel serverLevel)) return;

            Long firstShipId = getShipIdAtPos(serverLevel, firstPos);
            Long secondShipId = getShipIdAtPos(serverLevel, secondPos);

            RopeFactory.createNewRope(serverLevel, firstShipId, secondShipId, firstPos, secondPos, styleId, entity);

        } else {
            if (entity instanceof Player player) {
                player.displayClientMessage(
                        Component.translatable("vstuff.message.no_first_pos"),
                        true
                );
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (styleId != null)
            tag.put("style", TagUtils.writeResourceLocation(styleId));

        if (firstPos != null)
            tag.put("firstPos", NbtUtils.writeBlockPos(firstPos));

        if (firstDim != null)
            tag.putString("firstDim", firstDim);

    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("style"))
            styleId = TagUtils.readResourceLocation(tag.getCompound("style"));

        if (tag.contains("firstPos"))
            firstPos = NbtUtils.readBlockPos(tag.getCompound("firstPos"));

        if (tag.contains("firstDim"))
            firstDim = tag.getString("firstDim");

    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return super.getCapability(cap);
    }

    @SuppressWarnings("unchecked")
    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        EntityType.Builder<RopeArrowEntity> entityBuilder = (EntityType.Builder<RopeArrowEntity>) builder;
        return entityBuilder.sized(.5f, .5f);
    }

    public void setFirstPos(BlockPos pos) {
        this.firstPos = pos;
    }

    public void setFirstDim(String dim) {
        this.firstDim = dim;
    }

    public void setStyle(ResourceLocation styleId) {
        this.styleId = styleId;
    }

    public ResourceLocation getStyle() {
        return styleId;
    }


}

