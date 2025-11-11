package yay.evy.everest.vstuff.content.ponder;


import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;

public class RotationalThrusterPonder {

    public static void rotationalThruster(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("rotational_thruster", "Using the Rotational Thruster");

        scene.configureBasePlate(0, 0, 5);

        Selection base = util.select().fromTo(0, 0, 0, 4, 0, 4);
        scene.world().setBlocks(base, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.world().showSection(base, Direction.DOWN);
        scene.idle(20);

        BlockPos center = util.grid().at(2, 1, 2);
        BlockPos motor = center.west();

        scene.world().showSection(util.select().position(center), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
                .text(Component.translatable("vstuff.ponder.rotational_thruster.title").getString())
                .pointAt(util.vector().topOf(center))
                .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(util.select().position(motor), Direction.DOWN);
        scene.idle(15);
        scene.idle(15);

        BlockPos particle1 = center.above();
        BlockPos particle2 = center.above(2);

        scene.world().showSection(util.select().position(particle1), Direction.DOWN);
        scene.idle(2);
        scene.world().hideSection(util.select().position(particle1), Direction.UP);
        scene.world().showSection(util.select().position(particle2), Direction.DOWN);
        scene.idle(2);
        scene.world().hideSection(util.select().position(particle2), Direction.UP);



        scene.overlay().showText(70)
                .text(Component.translatable("vstuff.ponder.rotational_thruster.how").getString())
                .pointAt(util.vector().topOf(motor))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(80)
                .text(Component.translatable("vstuff.ponder.rotational_thruster.effect").getString())
                .pointAt(util.vector().blockSurface(center, Direction.NORTH))
                .placeNearTarget();
        scene.idle(90);
    }
}
