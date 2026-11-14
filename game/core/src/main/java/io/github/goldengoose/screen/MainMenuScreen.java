package io.github.goldengoose.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.goldengoose.Cog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.goldengoose.ui.UiStyles;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.assets.CogFonts;

public class MainMenuScreen implements Screen {

    final Cog game;

    private Stage stage;
    private BitmapFont menuFont;

    public MainMenuScreen(final Cog game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport(), game.batch);
        Gdx.input.setInputProcessor(stage);

        createMenu();
    }

    private void createMenu() {
        menuFont = Utilities.createFont(CogFonts.CRISTOFORO, 64);

        Label title = new Label("Call of Goosulhu", UiStyles.label(menuFont));
        TextButton playButton = new TextButton("PLAY", UiStyles.button(menuFont));
        TextButton quitButton = new TextButton("QUIT", UiStyles.button(menuFont));

        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(new CutsceneScreen(game));
            }
        });

        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        Table table = new Table();
        table.setFillParent(true);

        table.add(title).padBottom(50).colspan(2);
        table.row();

        table.add(playButton);
        table.add(quitButton);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

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
        menuFont.dispose();
    }

    // Unused overrides
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
