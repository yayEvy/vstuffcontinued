package yay.evy.everest.vstuff.content.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
// removed: import net.createmod.ponder.foundation.element.InputWindowElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import yay.evy.everest.vstuff.index.VStuffItems;

public class PhysPulleyPonder {

    public static void physPulley(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("phys_pulley", "The Phys Pulley");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.7f);
        scene.setSceneOffsetY(0);
        scene.removeShadow();

        ItemStack ropeItem = new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM);

        BlockPos baseStart = util.grid().at(0, 0, 0);
        BlockPos baseEnd = util.grid().at(4, 0, 4);
        BlockPos columnStart = util.grid().at(2, 1, 2);
        BlockPos columnEnd = util.grid().at(2, 5, 2);
        BlockPos pulleyPos = util.grid().at(2, 5, 1);
        BlockPos ironBlockPos = util.grid().at(2, 1, 1);
        BlockPos crankPos = util.grid().at(3, 5, 3);

        Selection basePlate = util.select().fromTo(baseStart, baseEnd);
        Selection oakColumn = util.select().fromTo(columnStart, columnEnd);
        Selection pulleySel = util.select().fromTo(pulleyPos, pulleyPos);
        Selection crankSel = util.select().fromTo(crankPos, crankPos);
        Selection ironSel = util.select().fromTo(ironBlockPos, ironBlockPos);


        scene.world().showSection(basePlate, Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(oakColumn, Direction.UP);
        scene.idle(20);

        scene.world().showSection(pulleySel, Direction.UP);
        scene.world().showSection(crankSel, Direction.UP);
        scene.idle(20);

        scene.world().showSection(ironSel, Direction.UP);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("vstuff.ponder.phys_pulley.text_1")
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("vstuff.ponder.phys_pulley.text_2")
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(100)
                .text("vstuff.ponder.phys_pulley.text_3")
                .placeNearTarget();

        scene.overlay().showControls(
                util.vector().topOf(pulleyPos),
                Pointing.DOWN,
                60
        ).rightClick().whileSneaking();

        scene.idle(100);

        scene.overlay().showText(100)
                .text("vstuff.ponder.phys_pulley.text_4")
                .placeNearTarget();

        scene.overlay().showControls(
                util.vector().topOf(ironBlockPos),
                Pointing.UP,
                60
        ).withItem(ropeItem).rightClick();

        scene.idle(100);

        scene.overlay().showLine(
                PonderPalette.GREEN,
                util.vector().centerOf(pulleyPos),
                util.vector().topOf(ironBlockPos),
                80
        );

        scene.overlay().showText(120)
                .text("vstuff.ponder.phys_pulley.text_5")
                .placeNearTarget();
        scene.idle(120);

        scene.markAsFinished();
    }
    // help
}
