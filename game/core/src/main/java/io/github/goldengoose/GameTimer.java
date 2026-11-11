package io.github.goldengoose;

/**
 * Countdown for the match. (UR_GAME_TIMER)
 */
public class GameTimer {
    private float secondsRemaining;

    public GameTimer(float seconds) {
        secondsRemaining = seconds;
    }

    public void update(float deltaTime) {
        secondsRemaining = Math.max(0f, secondsRemaining - deltaTime);
    }

    public boolean isTimeUp() { return secondsRemaining <= 0f; }

    public float getSecondsRemaining() { return secondsRemaining; }
}
