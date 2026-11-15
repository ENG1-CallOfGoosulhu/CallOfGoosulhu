package io.github.goldengoose.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * All the game's keyboard controls in one place: WASD to move, E to interact, T for the tome, and P or
 * Esc to pause.
 */
public class InputManager {
    public static boolean pause() {
        return Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }

    public static boolean tome() {
        return Gdx.input.isKeyJustPressed(Input.Keys.T);
    }

    public static boolean interact(){
        return Gdx.input.isKeyJustPressed(Input.Keys.E);
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
