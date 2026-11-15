package io.github.goldengoose.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.goldengoose.Cog;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.ui.UiStyles;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.assets.CogFonts;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import static io.github.goldengoose.assets.Assets.TUT_BUTTON;
import io.github.goldengoose.ui.TutMenu;
import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * The title screen, with Play, Quit and the tutorial book button.
 */
public class MainMenuScreen implements Screen {
    private static final float MENU_HEIGHT = 480f;
    private static final float MENU_WIDTH = 640f;

    private static final Color TEXT_COLOR = Color.valueOf("b5212d");
    private static final Color OUTLINE_COLOR = Color.valueOf("d6b2b5");
    private static final float OUTLINE_WIDTH = 2f;
    private static final Color SHADOW_COLOR = Color.valueOf("000000");
    private static final float SHADOW_OFFSET = 5f;

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
        stage = new Stage(new ExtendViewport(MENU_WIDTH, MENU_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);
        createMenu();
    }

    private void createMenu() {
        float toFontSize = 1080f / MENU_HEIGHT;
        menuFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, Math.round(64 * toFontSize),
            OUTLINE_WIDTH * toFontSize, TEXT_COLOR, OUTLINE_COLOR,
            SHADOW_OFFSET * toFontSize, SHADOW_COLOR);
        menuFont.getData().setScale(MENU_HEIGHT / Gdx.graphics.getDisplayMode().height);
        menuFont.setUseIntegerPositions(false);
        for (TextureRegion region : menuFont.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

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
                game.changeScreen(new StartCutsceneScreen(game));
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
        mainTable.add(title).colspan(2).expandY().top().padTop(6);
        mainTable.row();
        mainTable.add(playButton).padBottom(10);
        mainTable.add(quitButton).padBottom(10);

        Texture bgTexture = new Texture(Gdx.files.internal(Assets.TITLE_SCREEN));
        bgTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        Image background = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)), Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);

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
