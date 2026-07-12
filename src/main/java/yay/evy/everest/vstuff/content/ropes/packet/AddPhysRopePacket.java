package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.client.ClientPhysRopeManager;
import yay.evy.everest.vstuff.content.ropes.phys_ropes.ReworkedPhysRope;
import yay.evy.everest.vstuff.infrastructure.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.records.RopeSegment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AddPhysRopePacket extends SimplePacketBase {

    private Integer id;
    private Vector3d pos0;
    private Vector3d pos1;
    private Integer segmentCount;
    private List<RopeSegment> segments;
    private ResourceKey<RopeStyle> styleKey;

    public AddPhysRopePacket(ReworkedPhysRope rope) {
        this.id = rope.getRopeId();
        this.pos0 = rope.posData0.localPos();
        this.pos1 = rope.posData1.localPos();
        this.segmentCount = rope.segments.size();
        this.segments = new ArrayList<>(rope.segments);
        this.styleKey = rope.styleKey;
    }

    public AddPhysRopePacket(FriendlyByteBuf buffer) {
        this.id = buffer.readInt();
        this.pos0 = readVector3d(buffer);
        this.pos1 = readVector3d(buffer);
        this.segmentCount = buffer.readInt();
        this.segments = new ArrayList<>();
        for (int i = segmentCount; i > 0; i--) { // for loop of doom and despair
            this.segments.add(RopeSegment.readJsonFromBuffer(buffer));
        }
        this.styleKey = buffer.readResourceKey(VStuffRegistries.ROPE_STYLE);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(id);
        writeVector3d(buffer, pos0);
        writeVector3d(buffer, pos1);
        buffer.writeInt(segmentCount);
        for (RopeSegment segment : segments)
            RopeSegment.writeJsonToBuffer(buffer, segment);
        buffer.writeResourceKey(styleKey);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPhysRopeManager.addClientConstraint(id, pos0, pos1, segments, styleKey)));
        return false;
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
