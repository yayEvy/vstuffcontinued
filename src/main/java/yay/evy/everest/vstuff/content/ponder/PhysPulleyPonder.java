package yay.evy.everest.vstuff.content.ponder;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.index.VStuffItems;
//a
public class PhysPulleyPonder {

    public static void physPulley(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("vstuff.ponder.phys_pulley.title", "The Phys Pulley");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.7f);
        scene.setSceneOffsetY(0);
        scene.removeShadow();

        ItemStack ropeItem = new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM);

        BlockPos baseStart = util.grid.at(0, 0, 0);
        BlockPos baseEnd = util.grid.at(4, 0, 4);
        BlockPos columnStart = util.grid.at(2, 1, 2);
        BlockPos columnEnd = util.grid.at(2, 5, 2);
        BlockPos pulleyPos = util.grid.at(2, 5, 1); // north side
        BlockPos ironBlockPos = util.grid.at(2, 1, 1);
        BlockPos crankPos = util.grid.at(3, 5, 3); // side of pulley

        Selection basePlate = util.select.fromTo(baseStart, baseEnd);
        Selection oakColumn = util.select.fromTo(columnStart, columnEnd);
        Selection pulleySel = util.select.fromTo(pulleyPos, pulleyPos);
        Selection crankSel = util.select.fromTo(crankPos, crankPos);

        scene.world.showSection(basePlate, Direction.DOWN);
        scene.idle(40);
        scene.world.showSection(oakColumn, Direction.UP);
        scene.idle(40);

        scene.world.showSection(pulleySel, Direction.UP);
        scene.world.showSection(crankSel, Direction.UP);
        scene.idle(40);

        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(pulleyPos), Pointing.DOWN)
                .rightClick()
                .withItem(ropeItem), 40);
        scene.idle(40);

        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(pulleyPos), Pointing.DOWN)
                .rightClick()
                .whileSneaking(), 40);
        scene.idle(40);

        for (int yOffset = 0; yOffset < 3; yOffset++) {
            BlockPos ironStepPos = ironBlockPos.above(yOffset);
            Selection ironStepSel = util.select.fromTo(ironStepPos, ironStepPos);
            scene.world.showSection(ironStepSel, Direction.UP);

            scene.overlay.showControls(new InputWindowElement(util.vector.topOf(ironStepPos), Pointing.UP)
                    .rightClick()
                    .withItem(ropeItem), 40);

            scene.idle(40);
        }

        for (int y = ironBlockPos.getY(); y <= pulleyPos.getY(); y++) {
            Selection ropeStep = util.select.fromTo(util.grid.at(pulleyPos.getX(), y, pulleyPos.getZ()),
                    util.grid.at(pulleyPos.getX(), y, pulleyPos.getZ()));
            scene.world.showSection(ropeStep, Direction.UP);
            scene.idle(10);
        }

        scene.markAsFinished();
    }
}
