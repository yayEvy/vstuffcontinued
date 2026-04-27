package yay.evy.everest.vstuff.content.physics.levituff.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.index.VStuffSounds;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;

import java.util.*;

public class LevituffSoundPlayer {

    private int grindCooldown = 0;
    private int grindDuration = 0;
    private boolean grindActive = false;

    private int noteCooldown = randBetween(40, 120);

    private SoundEvent pendingNote = null;
    private int pendingNoteDelay = 0;

    private boolean playerNearbyCache = false;
    private int playerCheckCooldown = 0;
    private static final int PLAYER_CHECK_INTERVAL = 20;

    private static final SoundEvent[] NOTES = {
            VStuffSounds.LEVITUFF_D.get(),
            VStuffSounds.LEVITUFF_F.get(),
            VStuffSounds.LEVITUFF_G.get(),
            VStuffSounds.LEVITUFF_A.get(),
            VStuffSounds.LEVITUFF_C.get()
    };

    private final SoundEvent[] noteScratchBuffer = new SoundEvent[NOTES.length];

    private static final Random RAND = new Random();

    public void tick(Level level, BlockPos pos, int applierCount) {
        // i am so sorry :sob:
        if (!VStuffConfigs.client().levituffSounds.get()) {
            reset();
            return;
        }
        boolean playerNearby = getPlayerNearby(level, pos);

        tickGrind(level, pos, applierCount, playerNearby);
        tickNotes(level, pos, applierCount, playerNearby);
        tickPendingNote(level, pos);
    }

    private boolean getPlayerNearby(Level level, BlockPos pos) {
        if (playerCheckCooldown > 0) {
            playerCheckCooldown--;
            return playerNearbyCache;
        }
        playerCheckCooldown = PLAYER_CHECK_INTERVAL;
        playerNearbyCache = level.getNearestPlayer(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16.0, false
        ) != null;
        return playerNearbyCache;
    }

    private void tickGrind(Level level, BlockPos pos, int applierCount, boolean playerNearby) {
        if (!playerNearby) {
            grindActive = false;
            return;
        }
        if (grindActive) {
            grindDuration--;
            if (grindDuration <= 0) {
                grindActive = false;
                grindCooldown = randBetween(30, 80) * applierCount;
            }
        } else {
            grindCooldown--;
            if (grindCooldown <= 0) {
                grindActive = true;
                grindDuration = randBetween(40, 100);
                level.playLocalSound(
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        VStuffSounds.LEVITUFF_GRIND.get(),
                        SoundSource.BLOCKS,
                        0.18f, randPitch(0.85f, 1.05f), false
                );
            }
        }
    }

    private void tickNotes(Level level, BlockPos pos, int applierCount, boolean playerNearby) {
        if (!playerNearby) return;

        noteCooldown--;
        if (noteCooldown > 0) return;

        noteCooldown = randBetween(300, 500) * Math.max(1, applierCount);

        boolean twoNotes = RAND.nextInt(4) == 0;
        int noteCount = twoNotes ? 2 : 1;
        pickNotes(noteCount);

        playNote(level, pos, noteScratchBuffer[0]);

        if (twoNotes) {
            pendingNote = noteScratchBuffer[1];
            pendingNoteDelay = randBetween(8, 20);
        }
    }

    private void tickPendingNote(Level level, BlockPos pos) {
        if (pendingNote == null) return;
        pendingNoteDelay--;
        if (pendingNoteDelay <= 0) {
            playNote(level, pos, pendingNote);
            pendingNote = null;
        }
    }

    private void playNote(Level level, BlockPos pos, SoundEvent note) {
        level.playLocalSound(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                note, SoundSource.BLOCKS,
                0.12f, randPitch(0.95f, 1.05f), false
        );
    }

    private void pickNotes(int count) {
        System.arraycopy(NOTES, 0, noteScratchBuffer, 0, NOTES.length);
        for (int i = 0; i < count; i++) {
            int j = i + RAND.nextInt(NOTES.length - i);
            SoundEvent tmp = noteScratchBuffer[i];
            noteScratchBuffer[i] = noteScratchBuffer[j];
            noteScratchBuffer[j] = tmp;
        }
    }

    private static int randBetween(int min, int max) {
        return min + RAND.nextInt(max - min);
    }

    private static float randPitch(float min, float max) {
        return min + RAND.nextFloat() * (max - min);
    }

    public void reset() {
        grindActive = false;
        grindCooldown = 0;
        pendingNote = null;
        playerNearbyCache = false;
        playerCheckCooldown = 0;
    }
}