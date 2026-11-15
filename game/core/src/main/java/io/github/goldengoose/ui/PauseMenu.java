package io.github.goldengoose.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.screen.MainMenuScreen;
import io.github.goldengoose.assets.CogFonts;
import io.github.goldengoose.util.Utilities;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import static io.github.goldengoose.assets.Assets.TUT_BUTTON;
import io.github.goldengoose.assets.Assets;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class PauseMenu {
    private final Stage stage;
    private final BitmapFont titleFont = Utilities.createUiFont(CogFonts.UNCIALANTIQUA, 36);
    private final BitmapFont buttonFont = Utilities.createUiFont(CogFonts.UNCIALANTIQUA, 22);

    private final Table table = new Table();

    private TutMenu tutMenu;
    private boolean tutorialOpen = false;

    private Actor tutorialActor;
    private Table tutorialControls;

    public PauseMenu(Cog game) {
        stage = new Stage(new ExtendViewport(Utilities.UI_WIDTH, Utilities.UI_HEIGHT), game.batch);

        Label title = new Label("Press 'P' to unpause", UiStyles.label(titleFont));

        TextButton quitMenu = new TextButton("QUIT TO MENU", UiStyles.button(buttonFont));
        quitMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(new MainMenuScreen(game));
            }
        });

        TextButton quitDesktop = new TextButton("QUIT TO DESKTOP", UiStyles.button(buttonFont));
        quitDesktop.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        table.setFillParent(true);
        table.add(title).padBottom(50).colspan(2);
        table.row();
        table.add(quitMenu).padRight(20);
        table.add(quitDesktop).padLeft(20);

        Texture emptyToomeTexture = new Texture(Gdx.files.internal(Assets.SPELL_TOME_EMPTY));
        tutMenu = new TutMenu(emptyToomeTexture);
        tutorialActor = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                tutMenu.draw((SpriteBatch) batch, stage.getViewport());
            }
        };

        tutorialActor.setVisible(false);
        stage.addActor(tutorialActor);

        Texture tutTexture = new Texture(Gdx.files.internal(TUT_BUTTON));
        ImageButton tutButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(tutTexture)));

        tutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tutorialOpen = true;
                tutMenu.setOpen(true);
                table.setVisible(false);
                tutorialActor.setVisible(true);
                tutorialControls.setVisible(true);
            }
        });

        table.row();
        table.row();
        stage.addActor(table);
        tutButton.setSize(80, 80);
        tutButton.setPosition(20, 20);
        stage.addActor(tutButton);

        TextButton backButton = new TextButton("BACK", UiStyles.button(buttonFont));
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tutorialOpen = false;
                tutMenu.setOpen(false);
                tutorialActor.setVisible(false);
                tutorialControls.setVisible(false);
                table.setVisible(true);
            }
        });

        tutorialControls = new Table();
        tutorialControls.setFillParent(true);
        tutorialControls.bottom();
        tutorialControls.add(backButton).padBottom(20);
        tutorialControls.setVisible(false);

        stage.addActor(tutorialControls);
    }

    public boolean isTutorialOpen() { return tutorialOpen; }

    public void setVisible(boolean visible) { stage.getRoot().setVisible(visible); }

    public void closeTutorial() {
        tutorialOpen = false;
        tutMenu.setOpen(false);
        tutorialActor.setVisible(false);
        tutorialControls.setVisible(false);
        table.setVisible(true);
    }

    public Stage getStage() { return stage; }

    public void act(float delta) {
        stage.act(delta);
        if (tutorialOpen) {
            stage.getRoot().fire(new com.badlogic.gdx.scenes.scene2d.Event());
        }
    }
    public void draw() { stage.draw(); }

    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    public void dispose() {
        stage.dispose();
        titleFont.dispose();
        buttonFont.dispose();
    }
}
