package io.github.goldengoose.shared;

/**
 * Game timer, press p to pause and unpause. (UR\_GAME\_TIMER, UR\_PAUSE\_GAME)
 * Countdown for the match. (UR\_GAME\_TIMER)
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
