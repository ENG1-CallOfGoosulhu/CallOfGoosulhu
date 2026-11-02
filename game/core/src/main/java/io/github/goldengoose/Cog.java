package io.github.goldengoose;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.goldengoose.screen.MainMenuScreen;
import io.github.goldengoose.shared.Assets;

public class Cog extends Game {
    public SpriteBatch batch;
    public Assets assets;

    public FitViewport viewport;
    private Screen nextScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();
        assets.load();
        viewport = new FitViewport(80, 50);

        this.setScreen(new MainMenuScreen(this));
    }
    public void changeScreen(Screen screen) {
        nextScreen = screen;
    }

    @Override
    public void render() {
        if (nextScreen != null) {
            Screen oldScreen = getScreen();
            setScreen(nextScreen);
            nextScreen = null;

            if (oldScreen != null) {
                oldScreen.dispose();
            }
        }

        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }

        assets.dispose();
        batch.dispose();
    }
}
