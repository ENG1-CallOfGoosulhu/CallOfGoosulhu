package io.github.goldengoose.shared;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.screen.MainMenuScreen;
import io.github.goldengoose.shared.enums.CogFonts;

/** Game timer, press p to pause and unpause. (UR_GAME_TIMER, UR_PAUSE_GAME) */
/** Countdown for the match. (UR_GAME_TIMER) */
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
