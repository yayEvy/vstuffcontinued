package dev.flarelog.vstuff.index;

import dev.flarelog.vstuff.VStuff;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;

public class VStuffAdvancements {

    public static final ResourceLocation FIRST_ROPE =
            new ResourceLocation(VStuff.MOD_ID, "first_rope");

    public static final ResourceLocation FIRST_LEVITUFF =
            new ResourceLocation(VStuff.MOD_ID, "first_levituff");

    public static void awardFirstRope(ServerPlayer player) {
        Advancement advancement = player.server.getAdvancements()
                .getAdvancement(FIRST_ROPE);

        if (advancement == null) return;

        AdvancementProgress progress = player.getAdvancements()
                .getOrStartProgress(advancement);

        if (!progress.isDone()) {
            for (String criterion : progress.getRemainingCriteria()) {
                player.getAdvancements().award(advancement, criterion);
            }
        }

    }

    public static void awardFirstLevituff(ServerPlayer player) {
        Advancement advancement = player.server.getAdvancements()
                .getAdvancement(FIRST_LEVITUFF);

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