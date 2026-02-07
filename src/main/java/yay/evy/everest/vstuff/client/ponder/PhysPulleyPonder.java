package yay.evy.everest.vstuff.client.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.index.VStuffItems;

public class PhysPulleyPonder {

    public static void physPulley(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("phys_pulley", "The Phys Pulley");
        scene.configureBasePlate(0, 0, 5);
        scene.removeShadow();
        scene.scaleSceneView(0.7f);
        scene.setSceneOffsetY(-1);

        Selection all = util.select().fromTo(0, 0, 0, 4, 6, 4);
        scene.world().showSection(all, Direction.DOWN);
        scene.idle(10);

        BlockPos pulleyPos = util.grid().at(3, 6, 2);
        BlockPos anchorPos = util.grid().at(2, 2, 2);


        scene.overlay().showText(40)
                .text("vstuff.ponder.phys_pulley.text_rope")
                .pointAt(util.vector().centerOf(pulleyPos))
                .placeNearTarget();

        scene.overlay().showControls(
                        util.vector().topOf(pulleyPos),
                        Pointing.DOWN,
                        40
                )
                .withItem(new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get()))
                .rightClick();

        scene.idle(40);


        scene.overlay().showOutline(
                PonderPalette.GREEN,
                "anchor",
                util.select().position(anchorPos),
                50
        );

        scene.overlay().showText(40)
                .text("vstuff.ponder.phys_pulley.text_anchor")
                .pointAt(util.vector().topOf(anchorPos))
                .placeNearTarget();

        scene.idle(30);

        scene.overlay().showControls(
                        util.vector().topOf(anchorPos),
                        Pointing.DOWN,
                        40
                )
                .withItem(new ItemStack(VStuffItems.LEAD_CONSTRAINT_ITEM.get()))
                .rightClick();

        scene.idle(40);
        scene.markAsFinished();
    }


}
