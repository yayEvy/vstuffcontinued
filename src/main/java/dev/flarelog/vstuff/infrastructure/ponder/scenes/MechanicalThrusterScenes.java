package dev.flarelog.vstuff.infrastructure.ponder.scenes;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import dev.flarelog.vstuff.content.physics.ships.thrust.MechanicalThrusterBlock;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class MechanicalThrusterScenes {

    private static final BlockPos THRUSTER_1_POS = new BlockPos(2, 3, 2);
    private static final BlockPos THRUSTER_2_POS = new BlockPos(2, 1, 2);

    public static void mechanicalThruster1(SceneBuilder scene, SceneBuildingUtil util) {
        CreateSceneBuilder cscene = new CreateSceneBuilder(scene);

        scene.title("thruster_ponder_1", "Thruster basics");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1f);
        scene.setSceneOffsetY(-1);
        scene.removeShadow();

        BlockPos thrusterPos = THRUSTER_1_POS;
        Selection thrusterSelection = util.select().position(thrusterPos);

        Selection schematic = util.select()
                .fromTo(0, 0, 0, 4, 0, 4)
                .substract(thrusterSelection);

        scene.world().showSection(schematic, Direction.DOWN);
        scene.idle(10);

        scene.world().showSection(thrusterSelection, Direction.DOWN);
        cscene.world().setKineticSpeed(thrusterSelection, 0);
        scene.idle(15);

        scene.overlay().showText(60)
                .text("vstuff.ponder.thruster_ponder_1.text_1")
                .pointAt(util.vector().topOf(thrusterPos))
                .placeNearTarget();

        scene.idle(80);

        cscene.world().setKineticSpeed(thrusterSelection, 64);

        scene.overlay().showText(80)
                .text("vstuff.ponder.thruster_ponder_1.text_2")
                .pointAt(util.vector().blockSurface(thrusterPos, Direction.NORTH))
                .placeNearTarget();

        scene.idle(100);

        scene.overlay().showText(90)
                .text("vstuff.ponder.thruster_ponder_1.text_3")
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(150)
                .text("vstuff.ponder.thruster_ponder_1.text_4")
                .placeNearTarget();

        BlockPos currentPos = liftThruster(scene, cscene, util, thrusterPos, thrusterPos.above(1), 64);
        drawLineToGround(scene, util, currentPos, 50);
        scene.idle(60);

        currentPos = liftThruster(scene, cscene, util, currentPos, thrusterPos.above(3), 64);
        drawLineToGround(scene, util, currentPos, 50);
        scene.idle(60);

        cscene.world().setKineticSpeed(util.select().position(currentPos), 0);
        scene.idle(10);

        scene.markAsFinished();
    }


    private static BlockPos liftThruster(SceneBuilder scene, CreateSceneBuilder cscene, SceneBuildingUtil util, BlockPos from, BlockPos to, float speed) {
        Selection fromSel = util.select().position(from);
        Selection toSel = util.select().position(to);

        scene.world().hideSection(fromSel, Direction.UP);
        scene.idle(15);

        scene.world().setBlock(from, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(to, createThrusterState(Direction.DOWN, true), false);
        scene.world().showSection(toSel, Direction.DOWN);
        cscene.world().setKineticSpeed(toSel, speed);

        scene.idle(25);
        return to;
    }

    private static BlockState createThrusterState(Direction facing, boolean axisAlongFirst) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("vstuff", "mechanical_thruster"));
        BlockState state = block.defaultBlockState().setValue(MechanicalThrusterBlock.FACING, facing);

        Property<?> axisProp = block.getStateDefinition().getProperty("axis_along_first");
        if (axisProp instanceof BooleanProperty axisBool) {
            state = state.setValue(axisBool, axisAlongFirst);
        }
        return state;
    }


    private static void drawLineToGround(SceneBuilder scene, SceneBuildingUtil util, BlockPos currentThrusterPos, int ticks) {

        Vec3 thrusterBottom = util.vector().centerOf(currentThrusterPos).add(0, -0.5, 0);

        Vec3 ground = util.vector().topOf(currentThrusterPos.getX(), 0, currentThrusterPos.getZ());

        scene.overlay().showLine(
                PonderPalette.GREEN,
                thrusterBottom,
                ground,
                ticks
        );
    }

    public static void mechanicalThruster2(SceneBuilder scene, SceneBuildingUtil util) {
        CreateSceneBuilder cscene = new CreateSceneBuilder(scene);

        scene.title("thruster_ponder_2", "Thruster entity interaction");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1f);
        scene.setSceneOffsetY(-1);
        scene.removeShadow();

        BlockPos thrusterPos = THRUSTER_2_POS;
        Selection thrusterSelection = util.select().position(thrusterPos);

        Selection schematic = util.select()
                .fromTo(0, 0, 0, 4, 1, 4)
                .substract(thrusterSelection);

        scene.world().showSection(schematic, Direction.DOWN);
        scene.idle(10);

        scene.world().showSection(thrusterSelection, Direction.DOWN);
        cscene.world().setKineticSpeed(thrusterSelection, 0);
        scene.idle(15);

        scene.overlay().showText(60)
                .text("vstuff.ponder.thruster_ponder_2.text_1")
                .placeNearTarget();

        scene.idle(80);

        ElementLink<ParrotElement> birb =
                cscene.special().createBirb(
                        util.vector().topOf(thrusterPos),
                        ParrotPose.FacePointOfInterestPose::new
                );

        cscene.world().setKineticSpeed(thrusterSelection, 128);

        scene.overlay().showText(90)
                .text("vstuff.ponder.thruster_ponder_2.text_2")
                .placeNearTarget();

        // piss
        scene.special().moveParrot(
                birb,
                util.vector().of(0, 3, 0),
                100
        );

        scene.idle(100);

        scene.markAsFinished();
    }
}
