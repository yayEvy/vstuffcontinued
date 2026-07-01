package yay.evy.everest.vstuff.content.physics.levituff.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import yay.evy.everest.vstuff.index.VStuffBlocks;
import yay.evy.everest.vstuff.index.VStuffSounds;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;

import java.util.Random;

public class LevituffSoundPlayer {

    public static LevituffSoundPlayer get() { return INSTANCE; }
    public LevituffSoundPlayer() {}

    private boolean levituffNearby = false;
    private int scanCooldown = 0;
    private static final int SCAN_INTERVAL = 20;

    private static final Random RAND = new Random();

    private static int grindCooldown = randBetween(60, 120);
    private static int grindDuration = 0;
    private static boolean grindActive = false;
    private static long lastProcessedTick = -1L;

    private static int noteCooldown = randBetween(200, 400);

    private static SoundEvent pendingNote = null;
    private static int pendingNoteDelay = 0;

    private BlockPos lastFoundPos = null;

    private static final SoundEvent[] NOTES = {
            VStuffSounds.LEVITUFF_D.get(),
            VStuffSounds.LEVITUFF_F.get(),
            VStuffSounds.LEVITUFF_G.get(),
            VStuffSounds.LEVITUFF_A.get(),
            VStuffSounds.LEVITUFF_C.get()
    };

    private static final LevituffSoundPlayer INSTANCE = new LevituffSoundPlayer();

    private final SoundEvent[] noteScratchBuffer = new SoundEvent[NOTES.length];

    public void tick(Level level, BlockPos pos, int count) {
        if (!VStuffConfigs.client().levituffSounds.get()) {
            reset();
            return;
        }

        tickScan(level, pos);

        if (!levituffNearby || lastFoundPos == null) {
            grindActive = false;
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime == lastProcessedTick) return;
        lastProcessedTick = gameTime;

        tickGrind(level, pos);
        tickNotes(level, pos);
        tickPendingNote(level, pos);
    }

    private void tickScan(Level level, BlockPos playerPos) {
        if (scanCooldown-- > 0) return;
        scanCooldown = SCAN_INTERVAL;

        lastFoundPos = playerPos;
        levituffNearby = level.getBlockState(playerPos).is(VStuffBlocks.LEVITUFF.get());
    }

    private void tickGrind(Level level, BlockPos pos) {
        if (grindActive) {
            if (--grindDuration <= 0) {
                grindActive = false;
                grindCooldown = randBetween(80, 160);
            }
        } else {
            if (--grindCooldown <= 0) {
                grindActive = true;
                grindDuration = randBetween(40, 100);
                level.playLocalSound(
                        lastFoundPos.getX() + 0.5, lastFoundPos.getY() + 0.5, lastFoundPos.getZ() + 0.5,
                        VStuffSounds.LEVITUFF_GRIND.get(),
                        SoundSource.BLOCKS,
                        0.18f, randPitch(0.85f, 1.05f), false
                );
            }
        }
    }

    private void tickNotes(Level level, BlockPos pos) {
        if (--noteCooldown > 0) return;

        noteCooldown = randBetween(400, 700);

        boolean twoNotes = RAND.nextInt(4) == 0;
        pickNotes(twoNotes ? 2 : 1);

        playNote(level, pos, noteScratchBuffer[0]);

        if (twoNotes) {
            pendingNote = noteScratchBuffer[1];
            pendingNoteDelay = randBetween(8, 20);
        }
    }

    private void tickPendingNote(Level level, BlockPos pos) {
        if (pendingNote == null) return;
        if (--pendingNoteDelay <= 0) {
            playNote(level, pos, pendingNote);
            pendingNote = null;
        }
    }

    private void playNote(Level level, BlockPos pos, SoundEvent note) {
        if (lastFoundPos == null) return;
        level.playLocalSound(
                lastFoundPos.getX() + 0.5, lastFoundPos.getY() + 0.5, lastFoundPos.getZ() + 0.5,
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
        grindCooldown = randBetween(60, 120);
        grindDuration = 0;
        noteCooldown = randBetween(200, 400);
        pendingNote = null;
        pendingNoteDelay = 0;
        levituffNearby = false;
        scanCooldown = 0;
        lastFoundPos = null;
    }
}