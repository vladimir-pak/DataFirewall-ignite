package com.gpb.datafirewall.dto;

public record RefreshResult(
        boolean started,
        boolean success,
        boolean timeout,
        boolean alreadyRunning,
        long durationMs,
        String message
) {
    public static RefreshResult success(long durationMs) {
        return new RefreshResult(true, true, false, false, durationMs, "OK");
    }

    public static RefreshResult timeout(long timeoutMs) {
        return new RefreshResult(true, false, true, false, 0L,
                "Timeout after " + timeoutMs + " ms");
    }

    public static RefreshResult failed(String message) {
        return new RefreshResult(true, false, false, false, 0L, message);
    }

    public static RefreshResult running() {
        return new RefreshResult(false, false, false, true, 0L,
                "Уже работает");
    }
}
