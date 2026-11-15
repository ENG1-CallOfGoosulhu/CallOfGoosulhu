package io.github.goldengoose;

/**
 * UR_GAME_TIMER: "The game lasts up to 5 real world minutes"
 *
 * Counts down the 5-minute timer and says when time is up.
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
