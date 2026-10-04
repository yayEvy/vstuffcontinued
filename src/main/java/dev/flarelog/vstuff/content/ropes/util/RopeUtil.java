package dev.flarelog.vstuff.content.ropes.util;

import dev.flarelog.vstuff.content.ropes.VStuffRopeStyles;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.style.RopeStyleManager;
import dev.flarelog.vstuff.index.VStuffItems;
import dev.flarelog.vstuff.infrastructure.registry.VStuffBuiltInRegistries;
import dev.flarelog.vstuff.internal.utility.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import dev.flarelog.vstuff.content.ropes.Rope;
import dev.flarelog.vstuff.content.ropes.RopeManager;

import javax.annotation.Nullable;

public class RopeUtil {

    public static void addStyleToTag(ItemStack stack, ResourceKey<RopeStyle> style) {
        stack.getOrCreateTag().put("style", RopeStyle.keyToTag(style));
    }

    public static void createRopeDrop(Player player, ResourceKey<RopeStyle> style) {
        ItemStack ropeStack = new ItemStack(VStuffItems.ROPE.get());

        RopeUtil.addStyleToTag(ropeStack, style);

        player.drop(ropeStack, false);
    }

    public static void createRopeDrop(ServerLevel serverLevel, BlockPos pos, ResourceKey<RopeStyle> style) {
        Vector3d worldPos = PositionUtils.getWorldPos(serverLevel, pos);

        ItemStack ropeStack = new ItemStack(VStuffItems.ROPE.get());

        addStyleToTag(ropeStack, style);

        ItemEntity ropeDrop = new ItemEntity(
                serverLevel,
                worldPos.x,
                worldPos.y + 0.5,
                worldPos.z,
                ropeStack
        );

        serverLevel.addFreshEntity(ropeDrop);
    }

    public static void createRopeDrop(ServerLevel serverLevel, BlockPos pos) {
        createRopeDrop(serverLevel, pos, null);
    }

    public static void resetTag(ItemStack stack) {
        ResourceKey<RopeStyle> lastStyle = null;
        if (stack.getTag().contains("style")) {
            lastStyle = RopeStyle.tagToKey(stack.getTagElement("style"));
        }

        stack.setTag(null);

        if (lastStyle != null) {
            addStyleToTag(stack, lastStyle);
        }
        // clears tag then puts the style back if there was one
    }

    public static Component getRopeItemNameWithStyle(Item item, ItemStack stack) {
        if (stack.getTagElement("style") == null) addStyleToTag(stack, RopeStyleManager.DEFAULT_KEY);
        ResourceLocation location = RopeStyle.tagToKey(stack.getTagElement("style")).location();
        return Component.translatable(item.getDescriptionId(stack))
                .append(" (")
                .append(Component.translatable("ropestyle." + location.getNamespace() + "." + location.getPath())) // this should always be the correct translation key
                .append(")");
    }


    public static double getDistanceToRope(Vec3 eyePos, Vec3 lookVec, Vector3d ropeStart, Vector3d ropeEnd, double maxDistance) {
        Vec3 start = new Vec3(ropeStart.x, ropeStart.y, ropeStart.z);
        Vec3 end = new Vec3(ropeEnd.x, ropeEnd.y, ropeEnd.z);
        double minDistanceToRope = Double.MAX_VALUE;

        for (double t = 0; t <= maxDistance; t += 0.5) {
            Vec3 rayPoint = eyePos.add(lookVec.scale(t));
            Vec3 ropeVec = end.subtract(start);
            Vec3 startToRay = rayPoint.subtract(start);
            double ropeLength = ropeVec.length();
            if (ropeLength < 0.01) continue;

            double projection = startToRay.dot(ropeVec) / (ropeLength * ropeLength);
            projection = Math.max(0, Math.min(1, projection));

            Vec3 closestPointOnRope = start.add(ropeVec.scale(projection));
            double distanceToRope = rayPoint.distanceTo(closestPointOnRope);
            minDistanceToRope = Math.min(minDistanceToRope, distanceToRope);
        }

        return minDistanceToRope;
    }

    public static @Nullable Integer findRopeId(ServerLevel level, Player player) {
        Rope rope = findPhysRope(level, player);
        return rope == null ? null : rope.getRopeId();
    }

    public static @Nullable Rope findPhysRope(ServerLevel level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        double maxDistance = player.getBlockReach();
        double minDistance = Double.MAX_VALUE;
        Rope closest = null;

        for (Rope rope : RopeManager.get(level).getRopeList()) {
            Vector3d a = rope.posData0.getWorldPos(level);
            Vector3d b = rope.posData1.getWorldPos(level);

            double dist = getDistanceToRope(eyePos, lookVec, a, b, maxDistance);
            if (dist < minDistance && dist <= 1.0) {
                minDistance = dist;
                closest = rope;
            }
        }

        return closest;
    }

    public static void playSound(ServerLevel serverLevel, BlockPos pos, SoundEvent sound) {
        serverLevel.playSound(
                null,
                pos,
                sound,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );
    }
}