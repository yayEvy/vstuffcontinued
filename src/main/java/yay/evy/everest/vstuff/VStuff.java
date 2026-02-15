package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.client.VStuffClient;
import yay.evy.everest.vstuff.content.reaction_wheel.ReactionWheelAttachment;
import yay.evy.everest.vstuff.content.ropes.RopePersistence;
import yay.evy.everest.vstuff.content.physgrabber.PhysGrabberServerAttachment;
import yay.evy.everest.vstuff.content.ropes.thrower.RopeThrowerEntity;
import yay.evy.everest.vstuff.content.thrust.ThrusterForceAttachment;
import yay.evy.everest.vstuff.index.*;
import yay.evy.everest.vstuff.internal.network.NetworkHandler;
import yay.evy.everest.vstuff.internal.network.PhysGrabberNetwork;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.particles.ParticleTypes;
import org.valkyrienskies.core.api.VsBeta;

import static yay.evy.everest.vstuff.internal.utility.ShipUtils.getLoadedShipIdAtPos;
import static yay.evy.everest.vstuff.internal.utility.ShipUtils.getShipIdAtPos;

@Mod(VStuff.MOD_ID)
public class VStuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String NAME = "VStuff";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public VStuff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VStuffConfig.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, VStuffConfig.CLIENT_CONFIG);

        VStuffCreativeModeTabs.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);

        VStuffSounds.register(modEventBus);
        ParticleTypes.register(modEventBus);

        VStuffBlocks.register();
        VStuffItems.register();
        VStuffEntities.register(modEventBus);
        VStuffBlockEntities.register();
        VStuffPartials.register();

        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.registerPackets();
        PhysGrabberNetwork.register();

        modEventBus.addListener(this::commonSetup);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(VStuffClient::initialize);
        });

        ValkyrienSkiesMod.getApi().getShipLoadEvent().on(RopePersistence::onShipLoad);

        LOGGER.info("{} ({}) initialized", VStuff.NAME, VStuff.MOD_ID);
    }

    @VsBeta
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerAttachments();
            registerDispenserBehaviors();
        });
    }
    public static void registerAttachments() {
        LOGGER.info("Registering {} attachments...", VStuff.MOD_ID);

        // thruster attachment ! yippee
        ValkyrienSkiesMod.getApi().registerAttachment(
                ThrusterForceAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );

        // phys grabber aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
        ValkyrienSkiesMod.getApi().registerAttachment(
                PhysGrabberServerAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );

        // robbing john propulsion :3dsmile:
        ValkyrienSkiesMod.getApi().registerAttachment(
                ReactionWheelAttachment.class, builder -> {
                    builder.build();
                    return null;
                }
        );
    }


    public static void registerDispenserBehaviors() {
        DispenserBlock.registerBehavior(
                VStuffItems.ROPE_THROWER.get(),
                new AbstractProjectileDispenseBehavior() {

                    @Override
                    public ItemStack execute(BlockSource source, ItemStack stack) {
                        Level level = source.getLevel();
                        if (!(level instanceof ServerLevel serverLevel)) {
                            return stack;
                        }

                        BlockPos dispenserPos = source.getPos();
                        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);

                        Long startShipId = getLoadedShipIdAtPos(serverLevel, dispenserPos);

                        Position dispensePos = DispenserBlock.getDispensePosition(source);

                        RopeThrowerEntity rope = new RopeThrowerEntity(VStuffEntities.ROPE_THROWER.get(), serverLevel);
                        rope.setPos(dispensePos.x(), dispensePos.y(), dispensePos.z());

                        rope.setStartData(
                                dispenserPos,
                                startShipId,
                                serverLevel.dimension().location().toString(),
                                RopeUtils.ConnectionType.NORMAL,
                                null
                        );

                        rope.shoot(
                                direction.getStepX(),
                                direction.getStepY(),
                                direction.getStepZ(),
                                1.1F,
                                6.0F
                        );

                        serverLevel.addFreshEntity(rope);
                        stack.shrink(1);
                        return stack;
                    }

                    @Override
                    protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
                        return null;
                    }
                }
        );
    }


    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static ResourceLocation asTextureResource(String path) {
        return new ResourceLocation(MOD_ID, "textures/" + path);
    }

    public static ResourceLocation asModelResource(String path) {
        return new ResourceLocation(MOD_ID, "models/" + path);
    }

    public static MutableComponent translate(String key, Object... args) {
        Object[] args1 = LangBuilder.resolveBuilders(args);
        return Component.translatable(VStuff.MOD_ID + "." + key, args1);
    }

    public static <T extends Block> NonNullFunction<BlockBuilder<T, CreateRegistrate>, BlockBuilder<T, CreateRegistrate>> registerImpact(int speed) {
        return builder -> builder.onRegister(block -> BlockStressValues.setGeneratorSpeed(speed).accept(block));
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.sendClearAllConstraintsToPlayer(player);
        }
    }



}