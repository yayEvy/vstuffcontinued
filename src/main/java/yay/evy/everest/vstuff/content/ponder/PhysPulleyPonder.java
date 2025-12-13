package yay.evy.everest.vstuff.content.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.index.VStuffBlocks;
import yay.evy.everest.vstuff.index.VStuffItems;

public class PhysPulleyPonder {

    public static void physPulley(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("phys_pulley", "The Phys Pulley");
        scene.configureBasePlate(0, 0, 5);
        scene.removeShadow();
        scene.scaleSceneView(0.7f);
        scene.setSceneOffsetY(-1);


        // whatd yal have for breakfast, cuz i had bricks
        Selection all = util.select().fromTo(0, 0, 0, 4, 6, 4);
        scene.world().showSection(all, Direction.DOWN);
        scene.idle(20);

        BlockPos pulleyPos = util.grid().at(2, 6, 3);

        scene.overlay().showText(90)
                .text("vstuff.ponder.phys_pulley.text_rope")
                .placeNearTarget();

        scene.overlay().showControls(
                        util.vector().topOf(pulleyPos),
                        Pointing.DOWN,
                        60
                ).withItem(new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get()))
                .rightClick()
                .whileSneaking();

        scene.idle(120);
        scene.markAsFinished();
    }


}
