// RopeSoundPacket.java
package yay.evy.everest.vstuff.util.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.sound.RopeSoundHandler;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.function.Supplier;

public class RopeSoundPacket {
    private final boolean breakSound;
    private final RopeStyles.PrimitiveRopeStyle style;

    public RopeSoundPacket(boolean breakSound, RopeStyles.PrimitiveRopeStyle style) {
        this.breakSound = breakSound;
        this.style = style;
    }

    public static void encode(RopeSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.breakSound);
        buf.writeEnum(msg.style);
    }

    public static RopeSoundPacket decode(FriendlyByteBuf buf) {
        return new RopeSoundPacket(
                buf.readBoolean(),
                buf.readEnum(RopeStyles.PrimitiveRopeStyle.class)
        );
    }

    public static void handle(RopeSoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (RopeSoundHandler.isEnabled()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.level != null) {
                    var sound = msg.breakSound
                            ? (msg.style == RopeStyles.PrimitiveRopeStyle.CHAIN
                            ? SoundEvents.CHAIN_BREAK
                            : SoundEvents.LEASH_KNOT_BREAK)
                            : (msg.style == RopeStyles.PrimitiveRopeStyle.CHAIN
                            ? SoundEvents.CHAIN_PLACE
                            : SoundEvents.LEASH_KNOT_PLACE);

                    mc.level.playLocalSound(
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            sound,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F,
                            false
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

