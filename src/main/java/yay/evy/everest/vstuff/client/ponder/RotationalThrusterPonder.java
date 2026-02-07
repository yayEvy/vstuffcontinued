package yay.evy.everest.vstuff.client.ponder;


import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class RotationalThrusterPonder {

    public static void rotationalThruster(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("rotational_thruster", "Using the Rotational Thruster");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.7f);
        scene.setSceneOffsetY(-1);
        scene.removeShadow();

        // shooting create with bricks
        Selection all = util.select().fromTo(0, 0, 0, 4, 3, 4);
        scene.world().showSection(all, Direction.DOWN);
        scene.idle(40);

        BlockPos thrusterPos = util.grid().at(2, 1, 2);
        BlockPos motorPos = util.grid().at(1, 1, 2);

        scene.overlay().showText(30)
                .text("vstuff.ponder.rotational_thruster.title")
                .pointAt(util.vector().topOf(thrusterPos))
                .placeNearTarget();
        scene.idle(40);

        scene.overlay().showText(30)
                .text("vstuff.ponder.rotational_thruster.how")
                .pointAt(util.vector().topOf(motorPos))
                .placeNearTarget();
        scene.idle(40);

        scene.overlay().showText(30)
                .text("vstuff.ponder.rotational_thruster.effect")
                .pointAt(util.vector().blockSurface(thrusterPos, Direction.NORTH))
                .placeNearTarget();
        scene.idle(40);

        scene.markAsFinished();
    }

}
