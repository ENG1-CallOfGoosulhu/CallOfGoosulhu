package io.github.goldengoose.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.assets.CogFonts;
import io.github.goldengoose.ui.UiStyles;
import io.github.goldengoose.util.Utilities;

public class GameOverScreen implements Screen {

    private final Cog game;
    private final boolean won;
    private Stage stage;
    private BitmapFont font;

    public GameOverScreen(Cog game, boolean won) {
        this.game = game;
        this.won = won;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport(), game.batch);
        Gdx.input.setInputProcessor(stage);

        font = Utilities.createFont(CogFonts.CRISTOFORO, 64);

        String message = won ? "Congratulations!" : "Game Over";

        Label title = new Label(message, UiStyles.label(font));
        TextButton playAgain = new TextButton("PLAY AGAIN", UiStyles.button(font));
        TextButton menu = new TextButton("MENU", UiStyles.button(font));

        playAgain.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(new GameScreen(game));
            }
        });

        menu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(new MainMenuScreen(game));
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.add(title).padBottom(50).colspan(2);
        table.row();
        table.add(playAgain).padRight(20);
        table.add(menu);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(new Color(0f, 0f, 0f, 0.5f));
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
