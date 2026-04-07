package yay.evy.everest.vstuff.content.ropes.arrow;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.signature.qual.IdentifierOrPrimitiveType;
import yay.evy.everest.vstuff.VStuff;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;


public class RopeArrowRenderer extends ArrowRenderer<RopeArrowEntity> {

   public static final ResourceLocation texture = fromNamespaceAndPath(VStuff.MOD_ID, "textures/item/arrow.png"); // placeholder -Bry
    public RopeArrowRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(RopeArrowEntity pEntity) {
        return  texture;
    }





}
