package io.github.goldengoose.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Class to handle player input
 * <p>
 * TODO: Possibly store keybind settings and use those instead of hard-coded keys
 * TODO: Possibly allow arrow keys alongside WASD
 * </p>
 */
public class InputManager {
    public static boolean pause() {
        return Gdx.input.isKeyJustPressed(Input.Keys.P);
    }

    public static boolean isUp(boolean justPressed) {
        return justPressed ? Gdx.input.isKeyJustPressed(Input.Keys.W) : Gdx.input.isKeyPressed(Input.Keys.W);
    }

    public static boolean isDown(boolean justPressed) {
        return justPressed ? Gdx.input.isKeyJustPressed(Input.Keys.S) : Gdx.input.isKeyPressed(Input.Keys.S);
    }

    public static boolean isLeft(boolean justPressed) {
        return justPressed ? Gdx.input.isKeyJustPressed(Input.Keys.A) : Gdx.input.isKeyPressed(Input.Keys.A);
    }

    public static boolean isRight(boolean justPressed) {
        return justPressed ? Gdx.input.isKeyJustPressed(Input.Keys.D) : Gdx.input.isKeyPressed(Input.Keys.D);
    }

    public static float getHorizontal() {
        return (isRight(false) ? 1f : 0f)
            - (isLeft(false) ? 1f : 0f);
    }

    public static float getVertical() {
        return (isUp(false) ? 1f : 0f)
            - (isDown(false) ? 1f : 0f);
    }
}
