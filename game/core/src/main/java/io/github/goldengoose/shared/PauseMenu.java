package io.github.goldengoose.shared;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.screen.MainMenuScreen;
import io.github.goldengoose.shared.enums.CogFonts;

public class PauseMenu {
    private final Stage stage;
    private final BitmapFont titleFont = Utilities.createFont(CogFonts.CRISTOFORO, 48);
    private final BitmapFont buttonFont = Utilities.createFont(CogFonts.CRISTOFORO, 16);

    public PauseMenu(Cog game) {
        stage = new Stage(new ScreenViewport(), game.batch);

        Label title = new Label("Press 'P' to unpause", UiStyles.label(titleFont));

        TextButton quitMenu = new TextButton("QUIT TO MENU", UiStyles.button(buttonFont));
        quitMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(new MainMenuScreen(game));
            }
        });

        TextButton quitDesktop =
            new TextButton("QUIT TO DESKTOP", UiStyles.button(buttonFont));

        quitDesktop.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
                dispose();
            }
        });

        Table table = new Table();
        table.setFillParent(true);

        table.add(title).padBottom(50).colspan(2);
        table.row();

        table.add(quitMenu);
        table.add(quitDesktop);

        stage.addActor(table);
    }

    public Stage getStage() { return stage; }

    public void act(float delta) { stage.act(delta); }

    public void draw() { stage.draw(); }

    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    public void dispose() {
        stage.dispose();
        titleFont.dispose();
        buttonFont.dispose();
    }
}
