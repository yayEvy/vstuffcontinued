package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.index.VStuffSounds;

import java.util.*;

public class LevituffSoundPlayer {

    private int grindCooldown = 0;
    private int grindDuration = 0;
    private boolean grindActive = false;

    private int noteCooldown = randBetween(40, 120);

    private SoundEvent pendingNote = null;
    private int pendingNoteDelay = 0;

    private static final SoundEvent[] NOTES = {
            VStuffSounds.LEVITUFF_D.get(),
            VStuffSounds.LEVITUFF_F.get(),
            VStuffSounds.LEVITUFF_G.get(),
            VStuffSounds.LEVITUFF_A.get(),
            VStuffSounds.LEVITUFF_C.get()
    };

    private static final Random RAND = new Random();

    public void tick(Level level, BlockPos pos, int applierCount) {
        tickGrind(level, pos, applierCount);
        tickNotes(level, pos, applierCount);
        tickPendingNote(level, pos);
    }

    private void tickGrind(Level level, BlockPos pos, int applierCount) {
        if (!isPlayerNearby(level, pos)) {
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
                        0.18f,
                        randPitch(0.85f, 1.05f),
                        false
                );
            }
        }
    }

    private void tickNotes(Level level, BlockPos pos, int applierCount) {
        if (!isPlayerNearby(level, pos)) return;

        noteCooldown--;
        if (noteCooldown > 0) return;

        noteCooldown = randBetween(300, 500) * Math.max(1, applierCount);

        int noteCount = RAND.nextInt(4) == 0 ? 2 : 1;
        SoundEvent[] chosen = pickNotes(noteCount);

        playNote(level, pos, chosen[0]);

        if (noteCount == 2) {
            pendingNote = chosen[1];
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
                note,
                SoundSource.BLOCKS,
                0.12f,
                randPitch(0.95f, 1.05f),
                false
        );
    }

    private SoundEvent[] pickNotes(int count) {
        List<SoundEvent> pool = new ArrayList<>(Arrays.asList(NOTES));
        Collections.shuffle(pool, RAND);
        return pool.subList(0, count).toArray(new SoundEvent[0]);
    }

    private static int randBetween(int min, int max) {
        return min + RAND.nextInt(max - min);
    }

    private static float randPitch(float min, float max) {
        return min + RAND.nextFloat() * (max - min);
    }

    private boolean isPlayerNearby(Level level, BlockPos pos) {
        return level.getNearestPlayer(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16.0,
                false
        ) != null;
    }
}