// RopeSoundPacket.java
package yay.evy.everest.vstuff.util.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.sound.RopeSoundHandler;

import java.util.function.Supplier;

public class RopeSoundPacket {
    private final boolean breakSound;

    public RopeSoundPacket(boolean breakSound) {
        this.breakSound = breakSound;
    }

    public static void encode(RopeSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.breakSound);
    }

    public static RopeSoundPacket decode(FriendlyByteBuf buf) {
        return new RopeSoundPacket(buf.readBoolean());
    }


    public static void handle(RopeSoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (RopeSoundHandler.isEnabled()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.level.playLocalSound(
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            msg.breakSound ? SoundEvents.LEASH_KNOT_BREAK : SoundEvents.LEASH_KNOT_PLACE,
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
