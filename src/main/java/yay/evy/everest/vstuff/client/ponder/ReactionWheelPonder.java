package yay.evy.everest.vstuff.client.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ReactionWheelPonder {

    public static void reactionWheel(SceneBuilder scene, SceneBuildingUtil util) {
        CreateSceneBuilder cscene = new CreateSceneBuilder(scene);

        scene.title("reaction_wheel", "Using the Reaction Wheel");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.7f);
        scene.setSceneOffsetY(-1);
        scene.removeShadow();

        BlockPos wheelPos = util.grid().at(2, 1, 2);
        BlockPos motorPos = util.grid().at(2, 2, 2);

        Selection wheelSelection = util.select().position(wheelPos);

        Selection schematic = util.select().fromTo(0, 0, 0, 4, 3, 4)
                .substract(util.select().position(motorPos));
        scene.world().showSection(schematic, Direction.DOWN);

        cscene.world().setKineticSpeed(wheelSelection, 0);

        scene.idle(20);

        scene.overlay().showText(50)
                .text("This is the Reaction Wheel!")
                .pointAt(util.vector().topOf(wheelPos))
                .placeNearTarget();
        scene.idle(60);

        scene.overlay().showText(70)
                .text("It will produce Direct Torque when given rotation.")
                .pointAt(util.vector().topOf(wheelPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("The amount of force applied to the ship depends on the RPM and RPM direction.")
                .pointAt(util.vector().topOf(wheelPos))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(util.select().position(motorPos), Direction.DOWN);
        scene.idle(0);

        cscene.world().setKineticSpeed(wheelSelection, 64);
        cscene.idle(50);
        cscene.world().setKineticSpeed(wheelSelection, -64);
        cscene.idle(50);
        cscene.world().setKineticSpeed(wheelSelection, 128);
        cscene.idle(30);


        cscene.world().setKineticSpeed(wheelSelection, 0);
        scene.idle(40);

        scene.markAsFinished();
    }
}
