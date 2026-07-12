package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.client.ClientRopeStyle;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;

public class AddRopePacket extends SimplePacketBase {

    private Integer ropeId;
    private Long ship0;
    private Long ship1;
    private Vector3d localPos0;
    private Vector3d localPos1;
    private double maxLength;
    private ClientRopeStyle style;

    public AddRopePacket(ReworkedRope rope, ClientRopeStyle style) {
        this(rope.getRopeId(), rope.posData0.shipId(), rope.posData1.shipId(), rope.posData0.localPos(), rope.posData1.localPos(), rope.jointValues.maxLength(), style);
    }

    public AddRopePacket(Integer ropeId, Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, double maxLength, ClientRopeStyle style) {
        this.ropeId = ropeId;
        this.ship0 = ship0;
        this.ship1 = ship1;
        this.localPos0 = localPos0;
        this.localPos1 = localPos1;
        this.maxLength = maxLength;
        this.style = style;
    }

    public AddRopePacket(FriendlyByteBuf buffer) {
        this.ropeId = buffer.readInt();
        long tempId0 = buffer.readLong();
        long tempId1 = buffer.readLong();
        this.ship0 = tempId0 == -1 ? null : tempId0;
        this.ship1 = tempId1 == -1 ? null : tempId1;
        this.localPos0 = readVector3d(buffer);
        this.localPos1 = readVector3d(buffer);
        this.maxLength = buffer.readDouble();
        this.style = buffer.readJsonWithCodec(ClientRopeStyle.CODEC);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(ropeId);
        buffer.writeLong(ship0 == null ? -1 : ship0);
        buffer.writeLong(ship1 == null ? -1 : ship1);
        writeVector3d(buffer, localPos0);
        writeVector3d(buffer, localPos1);
        buffer.writeDouble(maxLength);
        buffer.writeJsonWithCodec(ClientRopeStyle.CODEC, style);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        //context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRopeManager.addClientConstraint(ropeId, ship0, ship1, localPos0, localPos1, maxLength, style)));
        return true;
    }

    private static Vector3d readVector3d(FriendlyByteBuf buf) {
        return new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private static void writeVector3d(FriendlyByteBuf buf, Vector3d vector3d) {
        buf.writeDouble(vector3d.x);
        buf.writeDouble(vector3d.y);
        buf.writeDouble(vector3d.z);
    }
}
