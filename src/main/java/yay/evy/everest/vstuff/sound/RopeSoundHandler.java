package yay.evy.everest.vstuff.sound;

public class RopeSoundHandler {
    private static boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }
}
