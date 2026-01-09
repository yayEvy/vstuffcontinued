package yay.evy.everest.vstuff.index;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.util.packet.OpenCreativeRopeEditorPacket;
import yay.evy.everest.vstuff.util.packet.RequestCreativeRopeEditorPacket;

public class VStuffPackets {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VStuff.MOD_ID, "packets"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int index = 0;

    public static void register() {
        CHANNEL.registerMessage(
                index++,
                RequestCreativeRopeEditorPacket.class,
                RequestCreativeRopeEditorPacket::encode,
                RequestCreativeRopeEditorPacket::decode,
                RequestCreativeRopeEditorPacket::handle
        );

        CHANNEL.registerMessage(
                index++,
                OpenCreativeRopeEditorPacket.class,
                OpenCreativeRopeEditorPacket::encode,
                OpenCreativeRopeEditorPacket::decode,
                OpenCreativeRopeEditorPacket::handle
        );
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}
