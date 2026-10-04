package dev.flarelog.vstuff.content.ropes;

public class RopeResult {
    public final Rope rope;
    public final boolean valid;
    public final String message;

    protected RopeResult(Rope rope, boolean valid, String message) {
        this.rope = rope;
        this.valid = valid;
        this.message = message;
    }

    public static RopeResult withMessage(String message) {
        return new RopeResult(null, false, message);
    }

    public static RopeResult validResult(Rope rope) {
        return new RopeResult(rope, true, null);
    }
}
