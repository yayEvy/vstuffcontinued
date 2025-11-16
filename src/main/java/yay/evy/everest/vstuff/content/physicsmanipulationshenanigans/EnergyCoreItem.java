package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class EnergyCoreItem extends Item {
    public EnergyCoreItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new EnergyCoreRenderer()));
    }
}
