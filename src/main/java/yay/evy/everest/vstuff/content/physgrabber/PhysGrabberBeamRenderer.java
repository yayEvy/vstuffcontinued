package yay.evy.everest.vstuff.content.physgrabber;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.core.api.ships.Ship;

public class PhysGrabberBeamRenderer {

    public static Ship grabbedShip;
    public static Vec3 playerEyePos;

    private static Vec3 smoothedShipPos = null;
    private static final float SHIP_SMOOTH_FACTOR = 0.08f;

    private static Vec3 smooth(Vec3 current, Vec3 target) {
        if (current == null) return target;
        double x = current.x + (target.x - current.x) * SHIP_SMOOTH_FACTOR;
        double y = current.y + (target.y - current.y) * SHIP_SMOOTH_FACTOR;
        double z = current.z + (target.z - current.z) * SHIP_SMOOTH_FACTOR;
        return new Vec3(x, y, z);
    }

    public static Vec3 getShipWorldPos(Ship ship) {
        if (ship == null) return null;
        var transform = ship.getTransform();
        var pos = transform.getPositionInWorld();
        return new Vec3(pos.x(), pos.y(), pos.z());
    }

    private static final RenderType BEAM_TYPE = RenderType.create(
            "phys_grabber_beam",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "alpha_transparency",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.blendFunc(
                                        GlStateManager.SourceFactor.SRC_ALPHA,
                                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                                );
                            },
                            () -> {
                                RenderSystem.disableBlend();
                                RenderSystem.defaultBlendFunc();
                            }
                    ))
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS))
                    .createCompositeState(false)
    );

    public static void render(PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        if (grabbedShip == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        Vec3 playerInterp = new Vec3(
                mc.player.xOld + (mc.player.getX() - mc.player.xOld) * partialTicks,
                mc.player.yOld + (mc.player.getY() - mc.player.yOld) * partialTicks + mc.player.getEyeHeight(),
                mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * partialTicks
        );

        Vec3 startWorld;
        if (mc.options.getCameraType().isFirstPerson()) {
            Vector3f camDir = new Vector3f(
                    (float) mc.gameRenderer.getMainCamera().getLookVector().x,
                    (float) mc.gameRenderer.getMainCamera().getLookVector().y,
                    (float) mc.gameRenderer.getMainCamera().getLookVector().z
            ).normalize();

            startWorld = playerInterp.add(camDir.x() * -0.3f, camDir.y() * -0.3f, camDir.z() * -0.3f);

            Vector3f camUp = new Vector3f(0, 1, 0);
            Vector3f camRight = new Vector3f();
            camDir.cross(camUp, camRight).normalize().mul(0.35f);
            startWorld = startWorld.add(camRight.x(), camRight.y(), camRight.z());
        } else {
            boolean isRightHand = mc.player.getMainArm() == HumanoidArm.RIGHT;
            Vec3 interpPos = new Vec3(
                    mc.player.xOld + (mc.player.getX() - mc.player.xOld) * partialTicks,
                    mc.player.yOld + (mc.player.getY() - mc.player.yOld) * partialTicks,
                    mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * partialTicks
            );
            Vec3 eyeOffset = new Vec3(0, mc.player.getEyeHeight(), 0);
            Vec3 handOffset = new Vec3(isRightHand ? 0.35 : -0.35, -0.2, 0.1);
            startWorld = interpPos.add(eyeOffset).add(handOffset);
        }

        Vec3 shipPos = getShipWorldPos(grabbedShip);
        if (shipPos == null) return;

        smoothedShipPos = smooth(smoothedShipPos, shipPos);

        Vec3 startRel = startWorld.subtract(camPos);
        Vec3 endRel = smoothedShipPos.subtract(camPos);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer buffer = buffers.getBuffer(BEAM_TYPE);

        float r = 0.75f, g = 0.4f, b = 1f, a = 0.8f;
        float distance = (float) startRel.distanceTo(endRel);
        float baseWidth = mc.options.getCameraType().isFirstPerson() ? 0.14f : 0.12f + distance * 0.015f;
        baseWidth = Math.min(baseWidth, 0.4f);

        Vector3f beamDir = new Vector3f((float) (endRel.x - startRel.x), (float) (endRel.y - startRel.y), (float) (endRel.z - startRel.z));
        if (beamDir.length() < 1e-6f) return;
        beamDir.normalize();

        Vector3f camDirVec = new Vector3f(
                (float) mc.gameRenderer.getMainCamera().getLookVector().x,
                (float) mc.gameRenderer.getMainCamera().getLookVector().y,
                (float) mc.gameRenderer.getMainCamera().getLookVector().z
        ).normalize();

        float viewDot = Math.abs(camDirVec.dot(beamDir));
        float alpha = a * (1.0f - 0.5f * viewDot);

        Vector3f right = new Vector3f();
        beamDir.cross(camDirVec, right);
        if (right.length() < 1e-6f) right.set(1, 0, 0);
        else right.normalize();

        Vector3f up = new Vector3f();
        right.cross(beamDir, up).normalize();

        int slices = 12;
        float pulse = 0.9f + 0.1f * (float) Math.sin((System.currentTimeMillis() % 2000L) / 2000f * Math.PI * 2);

        for (int i = 0; i < slices; i++) {
            float angle = (float) (i * Math.PI * 2.0 / slices);
            float nextAngle = (float) ((i + 1) * Math.PI * 2.0 / slices);

            Vector3f offset1 = new Vector3f(right).mul((float) Math.cos(angle)).add(new Vector3f(up).mul((float) Math.sin(angle))).mul(baseWidth * pulse);
            Vector3f offset2 = new Vector3f(right).mul((float) Math.cos(nextAngle)).add(new Vector3f(up).mul((float) Math.sin(nextAngle))).mul(baseWidth * pulse);

            Vector3f start1 = new Vector3f((float) startRel.x, (float) startRel.y, (float) startRel.z).add(offset1);
            Vector3f start2 = new Vector3f((float) startRel.x, (float) startRel.y, (float) startRel.z).add(offset2);
            Vector3f end1 = new Vector3f((float) endRel.x, (float) endRel.y, (float) endRel.z).add(offset1);
            Vector3f end2 = new Vector3f((float) endRel.x, (float) endRel.y, (float) endRel.z).add(offset2);

            float sliceAlpha = alpha * (0.8f + 0.2f * (float) Math.sin(System.currentTimeMillis() * 0.01 + i));

            buffer.vertex(matrix, start1.x, start1.y, start1.z).color(r, g, b, sliceAlpha).endVertex();
            buffer.vertex(matrix, start2.x, start2.y, start2.z).color(r, g, b, sliceAlpha).endVertex();
            buffer.vertex(matrix, end2.x, end2.y, end2.z).color(r, g, b, sliceAlpha).endVertex();
            buffer.vertex(matrix, end1.x, end1.y, end1.z).color(r, g, b, sliceAlpha).endVertex();
        }
    }
}
