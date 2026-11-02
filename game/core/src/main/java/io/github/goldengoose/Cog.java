package io.github.goldengoose;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.goldengoose.screen.MainMenuScreen;
import io.github.goldengoose.shared.Assets;

public class Cog extends Game {
    public SpriteBatch batch;
    public Assets assets;

    private Screen nextScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();
        assets.load();

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
