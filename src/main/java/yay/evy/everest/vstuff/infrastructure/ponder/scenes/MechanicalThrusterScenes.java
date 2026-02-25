package yay.evy.everest.vstuff.infrastructure.ponder.scenes;


import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.phys.AABB;
import net.createmod.ponder.api.element.ParrotPose;
import net.minecraft.world.phys.Vec3;

public class MechanicalThrusterScenes {

    public static void mechanicalThruster(SceneBuilder scene, SceneBuildingUtil util) {
        CreateSceneBuilder cscene = new CreateSceneBuilder(scene);

        scene.title("mechanical_thruster", "Using the Mechanical Thruster");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.7f);
        scene.setSceneOffsetY(-1);
        scene.removeShadow();
        BlockPos thrusterPos = util.grid().at(2, 1, 3);
        BlockPos parrotPos = util.grid().at(0, 1, 3); // stupid fucking thing,  was 2 now 3
        BlockPos geraldPos = util.grid().at(4, 1, 4);
        Selection thrusterSelection = util.select().position(thrusterPos);
        cscene.world().setKineticSpeed(thrusterSelection, 0);


        // 2 1 4
        BlockPos motorPos = util.grid().at(2, 1, 4);

        Selection schematic = util.select().fromTo(0, 0, 0, 4, 3, 4)
                .substract(util.select().position(motorPos));
        scene.world().showSection(schematic, Direction.DOWN);
        ElementLink<ParrotElement> birb =
                cscene.special().createBirb(
                        util.vector().centerOf(geraldPos).add(0, 0, 0),
                        () -> new ParrotPose() {
                            @Override
                            public void tick(PonderScene scene, Parrot entity, Vec3 location) {
                            }
                        }
                );
        scene.overlay().showText(60)
                .text("vstuff.ponder.mechanical_thruster.title")
                .pointAt(util.vector().topOf(thrusterPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(80)
                .text("vstuff.ponder.mechanical_thruster.how")
                .pointAt(util.vector().topOf(motorPos))
                .placeNearTarget();
        scene.idle(40);
        scene.rotateCameraY(180);
        scene.world().showSection(util.select().position(motorPos), Direction.DOWN);
        scene.idle(40);
        scene.rotateCameraY(-180);

        scene.idle(20);


        cscene.world().setKineticSpeed(thrusterSelection, 64);
        scene.overlay().showText(80)
                .text("vstuff.ponder.mechanical_thruster.effect")
                .pointAt(util.vector().blockSurface(thrusterPos, Direction.NORTH))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(90)
                .text("vstuff.ponder.mechanical_thruster.text_4")
                .placeNearTarget();

        cscene.world().setKineticSpeed(thrusterSelection, 0);

        // push parrots yeah
        ElementLink<ParrotElement> birb2 =
                cscene.special().createBirb(
                        util.vector().centerOf(parrotPos).add(0, 0, 0),
                        () -> new ParrotPose() {
                            @Override
                            public void tick(PonderScene scene, Parrot entity, Vec3 location) {
                            }
                        }
                );

        cscene.world().setKineticSpeed(thrusterSelection, 128);

        scene.special().moveParrot(
                birb2,
                util.vector().of(-2, 0, 0),
                100
        );


        scene.idle(90);



        scene.markAsFinished();
    }

}
