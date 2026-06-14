package yay.evy.everest.vstuff.content.ropes.phys_ropes;

import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.internal.utility.GTPAUtils;
import yay.evy.everest.vstuff.internal.utility.records.RopePosData;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;

import java.util.ArrayList;
import java.util.List;

public class PhysRope {

    public int physRopeId;
    public List<Vector3d> lastKnownSegmentPositions = new ArrayList<>();

    public final RopePosData posData0;
    public final RopePosData posData1;

    public final List<ServerVsBody> segmentBodies = new ArrayList<>();
    public final List<Integer> jointIds = new ArrayList<>();
    public final List<Integer> clientRopeIds = new ArrayList<>();

    public final RopeStyle style;

    public final float segmentLength;

    public PhysRope(RopePosData posData0, RopePosData posData1, RopeStyle style, float segmentLength) {
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.style = style;
        this.segmentLength = segmentLength;
    }

    public void detach(ServerLevel level) {
        posData0.remove(level, physRopeId);
        posData1.remove(level, physRopeId);

        for (Integer clientId : clientRopeIds) {
            ClientRopeManager.removeClientConstraint(clientId);
        }
        clientRopeIds.clear();
    }

    public void attach(ServerLevel level) {
        posData0.attach(level, physRopeId);
        posData1.attach(level, physRopeId);
    }
}