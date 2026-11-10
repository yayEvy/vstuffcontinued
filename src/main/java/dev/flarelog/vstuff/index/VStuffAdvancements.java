package dev.flarelog.vstuff.index;

import dev.flarelog.vstuff.VStuff;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;

public class VStuffAdvancements {

    public static final ResourceLocation FIRST_ROPE = VStuff.asResource("first_rope");

    public static final ResourceLocation FIRST_LEVITUFF = VStuff.asResource("first_levituff");

    public static void awardFirstRope(ServerPlayer player) {
       award(player, FIRST_ROPE);
    }

    public static void awardFirstLevituff(ServerPlayer player) {
        award(player, FIRST_LEVITUFF);
    }

    private static void award(ServerPlayer player, ResourceLocation location) {
        Advancement advancement = player.server.getAdvancements()
                .getAdvancement(location);

        if (advancement == null) return;

        AdvancementProgress progress = player.getAdvancements()
                .getOrStartProgress(advancement);

        if (!progress.isDone()) {
            for (String criterion : progress.getRemainingCriteria()) {
                player.getAdvancements().award(advancement, criterion);
            }
        }
    }
}