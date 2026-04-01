// RopeSoundPacket.java
package yay.evy.everest.vstuff.internal.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.sound.RopeSoundHandler;

import java.util.function.Supplier;

public class RopeSoundPacket {
    private final boolean breakSound;
    private final boolean chain;

    public RopeSoundPacket(boolean breakSound, boolean chain) {
        this.breakSound = breakSound;
        this.chain = chain;
    }

    public static void encode(RopeSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.breakSound);
        buf.writeBoolean(msg.chain);
    }

    public static RopeSoundPacket decode(FriendlyByteBuf buf) {
        return new RopeSoundPacket(
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    public static void handle(RopeSoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (RopeSoundHandler.isEnabled()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.level != null) {
                    var sound = msg.breakSound
                            ? (msg.chain
                            ? SoundEvents.CHAIN_BREAK
                            : SoundEvents.WOOL_BREAK)
                            : (msg.chain
                            ? SoundEvents.CHAIN_PLACE
                            : SoundEvents.WOOL_PLACE);

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

