package io.github.goldengoose.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.goldengoose.Cog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.ui.UiStyles;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.assets.CogFonts;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import static io.github.goldengoose.assets.Assets.TUT_BUTTON;
import io.github.goldengoose.ui.TutMenu;
import com.badlogic.gdx.graphics.g2d.Batch;

public class MainMenuScreen implements Screen {

    final Cog game;

    private Stage stage;
    private BitmapFont menuFont;
    private TutMenu tutMenu;
    private Actor tutorialActor;
    private Table mainTable;
    private ImageButton tutButton;

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
        menuFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, 64);

        Label title = new Label("Call of Goosulhu", UiStyles.label(menuFont));
        TextButton playButton = new TextButton("PLAY", UiStyles.button(menuFont));
        TextButton quitButton = new TextButton("QUIT", UiStyles.button(menuFont));
        Texture tutTexture = new Texture(Gdx.files.internal(TUT_BUTTON));
        tutTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        tutButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(tutTexture)));

        Texture emptyTomeTexture = new Texture(Gdx.files.internal(Assets.SPELL_TOME_EMPTY));
        tutMenu = new TutMenu(emptyTomeTexture);

        tutorialActor = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                if (tutMenu.isOpen()) {
                    tutMenu.draw((SpriteBatch) batch, stage.getViewport());
                }
            }
        };

        tutorialActor.setSize(0, 0);
        Table tutorialControls = new Table();
        tutorialControls.setFillParent(true);
        tutorialControls.bottom();

        TextButton backButton = new TextButton("BACK", UiStyles.button(menuFont));
        tutorialControls.add(backButton).padBottom(20);
        tutorialControls.setVisible(false);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tutMenu.setOpen(false);
                tutorialControls.setVisible(false);
            }
        });

        tutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tutMenu.setOpen(true);
                tutorialControls.setVisible(true);
            }
        });

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

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.add(title).padBottom(50).colspan(2);
        mainTable.row();
        mainTable.add(playButton);
        mainTable.add(quitButton);

        stage.addActor(mainTable);

        tutButton.setSize(80, 80);
        tutButton.setPosition(20, 20);
        stage.addActor(tutButton);

        stage.addActor(tutorialActor);
        stage.addActor(tutorialControls);
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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
