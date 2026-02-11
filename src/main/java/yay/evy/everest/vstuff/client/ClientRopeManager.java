package yay.evy.everest.vstuff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.internal.RopeStyle;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRopeManager {
    private static final Map<Integer, ClientRopeData> clientConstraints = new HashMap<>();
    private static ClientRopeData translucentRope = null;

    public record ClientRopeData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB, double maxLength,
                                 ResourceLocation style) {
            public ClientRopeData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB, double maxLength, ResourceLocation style) {
                this.shipA = shipA;
                this.shipB = shipB;
                this.localPosA = new Vector3d(localPosA);
                this.localPosB = new Vector3d(localPosB);
                this.maxLength = maxLength;
                this.style = style;
            }

            public Vector3d getWorldPosA(Level level, float partialTick) {
                try {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level == null) return new Vector3d(localPosA);

                    if (shipA == null || shipA == 0L) {
                        return new Vector3d(localPosA);
                    } else {
                        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipA);
                        if (shipObject != null) {
                            Vector3d worldPos = new Vector3d();


                            try {
                                ((ClientShip) shipObject).getRenderTransform().getShipToWorld().transformPosition(localPosA, worldPos);
                            } catch (Exception e) {
                                shipObject.getTransform().getShipToWorld().transformPosition(localPosA, worldPos);
                            }

                            return worldPos;
                        }
                    }
                    return new Vector3d(localPosA);
                } catch (Exception e) {
                    return new Vector3d(localPosA);
                }
            }

            public Vector3d getWorldPosB(Level level, float partialTick) {
                try {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level == null) return new Vector3d(localPosB);

                    if (shipB == null || shipB == 0L) {
                        return new Vector3d(localPosB);
                    } else {
                        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipB);
                        if (shipObject != null) {
                            Vector3d worldPos = new Vector3d();

                            try {
                                ((ClientShip) shipObject).getRenderTransform().getShipToWorld().transformPosition(localPosB, worldPos);
                            } catch (Exception e) {
                                shipObject.getTransform().getShipToWorld().transformPosition(localPosB, worldPos);
                            }

                            return worldPos;
                        }
                    }
                    return new Vector3d(localPosB);
                } catch (Exception e) {
                    return new Vector3d(localPosB);
                }
            }

            public Vector3d getRawWorldPosA(Level level) {
                try {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level == null) return new Vector3d(localPosA);

                    if (shipA == null || shipA == 0L) {
                        return new Vector3d(localPosA);
                    } else {
                        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(mc.level).getAllShips().getById(shipA);
                        if (shipObject != null) {
                            Vector3d worldPos = new Vector3d();
                            shipObject.getTransform().getShipToWorld().transformPosition(localPosA, worldPos);
                            return worldPos;
                        }
                    }
                    return new Vector3d(localPosA);
                } catch (Exception e) {
                    return new Vector3d(localPosA);
                }
            }

            public Vector3d getRawWorldPosB(Level level) {
                try {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level == null) return new Vector3d(localPosB);

                    if (shipB == null || shipB == 0L) {
                        return new Vector3d(localPosB);
                    } else {
                        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(mc.level).getAllShips().getById(shipB);
                        if (shipObject != null) {
                            Vector3d worldPos = new Vector3d();
                            shipObject.getTransform().getShipToWorld().transformPosition(localPosB, worldPos);
                            return worldPos;
                        }
                    }
                    return new Vector3d(localPosB);
                } catch (Exception e) {
                    return new Vector3d(localPosB);
                }
            }
        public boolean isRenderable(Level level) {
            if (level == null) return false;

            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
            if (shipWorld == null) return false;

            if (shipA != null && shipA != 0L) {
                Ship a = shipWorld.getAllShips().getById(shipA);
                if (!(a instanceof ClientShip csA)) return false;
                if (csA.getRenderTransform() == null) return false;
            }

            if (shipB != null && shipB != 0L) {
                Ship b = shipWorld.getAllShips().getById(shipB);
                if (!(b instanceof ClientShip csB)) return false;
                if (csB.getRenderTransform() == null) return false;
            }

            return true;
        }

    }

    public static boolean hasPreviewRope() {
        return !(translucentRope == null);
    }

    public static void setPreviewRope(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, float maxLength, ResourceLocation style) {
        translucentRope = new ClientRopeData(ship0, ship1, localPos0, localPos1, maxLength, style);
    }

    public static void clearPreviewRope() {
        translucentRope = null;
    }

    public static void addClientConstraint(Integer constraintId, Long shipA, Long shipB,
                                           Vector3d localPosA, Vector3d localPosB, double maxLength, ResourceLocation style) {
        clientConstraints.put(constraintId, new ClientRopeData(shipA, shipB, localPosA, localPosB, maxLength, style));
    }

    public static void removeClientConstraint(Integer constraintId) {
        if (constraintId == null) return;

        clientConstraints.remove(constraintId);


        RopeRendererClient.positionCache.remove(constraintId);
    }

    public static Map<Integer, ClientRopeData> getClientConstraints() {
        return clientConstraints;
    }

    public static void clearAllClientConstraints() {
        clientConstraints.clear();
        translucentRope = null;
        RopeRendererClient.clearCache();
    }

}