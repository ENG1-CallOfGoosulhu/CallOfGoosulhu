package io.github.goldengoose.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.assets.CogFonts;
import io.github.goldengoose.ui.Hud;
import io.github.goldengoose.ui.PauseMenu;
import io.github.goldengoose.ui.TomeMenu;
import io.github.goldengoose.util.InputManager;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.world.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.Color;

public class GameScreen implements Screen {
    private final Cog game;
    private final Viewport worldViewport = new FitViewport(600, 450);
    private final Viewport hudViewport = new ScreenViewport();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final InputMultiplexer multiplexer = new InputMultiplexer();

    private final Vector2 mouseWorldPosition = new Vector2();
    private final Vector2 playerPos = new Vector2();

    private final PauseMenu pauseMenu;
    private final TomeMenu tomeMenu;
    private final BitmapFont hudFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, 24);
    private final BitmapFont objectiveFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, 40);    private final Hud hud;
    private final Rectangle viewRect = new Rectangle();
    private final GameSession session;

    private boolean paused = true;
    private boolean tomeOpen = false;
    private boolean debug = false;
    private boolean overlayVisible = false;
    private boolean overlayWon = false;
    private float overlaySlide = 0f;
    private BitmapFont overlayFont;
    private float gameOverTimer = -1f;
    private boolean gameOverWon = false;

    public GameScreen(final Cog game) {
        this.game = game;
        session = new GameSession(game.assets, worldViewport, "Outside_campus_cthulhu_props", "start");
        pauseMenu = new PauseMenu(game);
        tomeMenu = new TomeMenu(game, game.assets.getTexture(Assets.SPELL_TOME_EMPTY), session.getSpellTome());
        hud = new Hud(game.assets, 5 * 60);
        multiplexer.addProcessor(pauseMenu.getStage());
        multiplexer.addProcessor(session.getSpellTome().getInputProcessor());
        multiplexer.addProcessor(gameInput);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        if (!tomeOpen && InputManager.pause()) {
            paused = !paused;
            pauseMenu.setVisible(paused);
            pauseMenu.closeTutorial();
        }
        if ((paused == tomeOpen) && InputManager.tome()) {
            tomeOpen = !tomeOpen;
            paused = tomeOpen;
            session.getSpellTome().setVisible(!tomeOpen);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) debug = !debug;

        if (!paused) {
            session.update(delta);
            hud.update(delta, session.getTimer().getSecondsRemaining());
            hud.updatePatience(session.getPatienceSystem().getPatience());

            if (session.getTimer().isTimeUp() && gameOverTimer < 0) {
                gameOverTimer = 2f;
                gameOverWon = false;
            }
            if (session.getPatienceSystem().isTransformed() && gameOverTimer < 0) {
                session.getPlayer().transformIntoGoose(game.assets.getTexture(Assets.GOOSE_WALK));
                gameOverTimer = 2f;
                gameOverWon = false;
            }
            if (gameOverTimer > 0) {
                gameOverTimer -= delta;
                if (gameOverTimer <= 0) {
                    showOverlay(gameOverWon);
                }
            }
        }

        session.getPlayer().getCentre(playerPos);
        OrthographicCamera camera = (OrthographicCamera) worldViewport.getCamera();
        camera.position.set(playerPos.x, playerPos.y, 0f);
        worldViewport.apply();

        viewRect.set(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth, camera.viewportHeight);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        session.getArea().render(game.batch, camera, viewRect);

        if (debug) renderDebug();

        hudViewport.apply();
        hud.draw(game.batch, hudViewport, hudFont, session.getSpellTome(), session.getKeyState());

        shapes.setProjectionMatrix(hudViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        session.getSpellTome().drawShapes(shapes, hudViewport);
        shapes.end();

        game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
        game.batch.begin();
        session.getSpellTome().drawSprites(game.batch, hudViewport);
        game.batch.end();

        drawObjective();

        if (paused && !overlayVisible) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.setProjectionMatrix(hudViewport.getCamera().combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.5f);
            shapes.rect(0, 0, hudViewport.getWorldWidth(), hudViewport.getWorldHeight());
            shapes.end();
            pauseMenu.act(delta);
            pauseMenu.draw();

            if (pauseMenu.isTutorialOpen()) {
                session.getSpellTome().setVisible(false);
            } else {
                session.getSpellTome().setVisible(true);
            }
        }

        if (tomeOpen) {
            game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
            game.batch.begin();
            tomeMenu.draw(game.batch, hudViewport);
            game.batch.end();
        }

        if (overlayVisible) {
            overlaySlide = Math.min(overlaySlide + delta * 2f, 1f);
            float w = hudViewport.getWorldWidth();
            float h = hudViewport.getWorldHeight();
            float panelH = h;
            float panelY = h - overlaySlide * panelH;

            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.setProjectionMatrix(hudViewport.getCamera().combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.7f);
            shapes.rect(0, panelY, w, panelH);
            shapes.end();

            String message = overlayWon ? "Congratulations!\nYou Escaped The Goosulhu!" : "Better Luck Next Time";
            game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
            game.batch.begin();
            overlayFont.draw(game.batch, message, 0, panelY + panelH * 0.6f, w, Align.center, false);
            overlayFont.draw(game.batch, "'R' to Play Again      'M' to Menu", 0, panelY + panelH * 0.35f, w, Align.center, false);
            game.batch.end();
        }

        if (overlayVisible) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                game.changeScreen(new GameScreen(game));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                game.changeScreen(new MainMenuScreen(game));
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void drawObjective() {
        ObjectiveSystem obj = session.getObjectiveSystem();
        if (!obj.isVisible()) return;

        float w = hudViewport.getWorldWidth();
        float h = hudViewport.getWorldHeight();
        float alpha = obj.getAlpha();
        float textY = h * 0.95f;
        String text = obj.getCurrentObjective();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
        game.batch.begin();

        objectiveFont.setColor(1f, 1f, 1f, alpha);
        objectiveFont.draw(game.batch, text, -2, textY, w, Align.center, false);
        objectiveFont.draw(game.batch, text,  2, textY, w, Align.center, false);
        objectiveFont.draw(game.batch, text,  0, textY + 2, w, Align.center, false);
        objectiveFont.draw(game.batch, text,  0, textY - 2, w, Align.center, false);

        objectiveFont.setColor(0f, 0f, 0f, alpha);
        objectiveFont.draw(game.batch, text, 0, textY, w, Align.center, false);

        objectiveFont.setColor(Color.WHITE);
        game.batch.end();
    }

    private void showOverlay(boolean won) {
        if (!overlayVisible) {
            overlayVisible = true;
            overlayWon = won;
            overlayFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, 64);
            paused = true;
        }
    }

    private final InputAdapter gameInput = new InputAdapter() {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (paused) return false;
            if (button == Input.Buttons.LEFT) {
                worldViewport.unproject(mouseWorldPosition.set(screenX, screenY));
                if (!session.clickDoor(mouseWorldPosition)) {
                    session.getSpellTome().activateSelectedSpell(mouseWorldPosition);
                }
                return true;
            }
            return false;
        }
    };

    private void renderDebug() {
        shapes.setProjectionMatrix(worldViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        session.getWorld().renderDebug(shapes);
        shapes.end();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height);
        hudViewport.update(width, height, true);
        pauseMenu.resize(width, height);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        pauseMenu.dispose();
        tomeMenu.dispose();
        hudFont.dispose();
        objectiveFont.dispose();
        hud.dispose();
        if (overlayFont != null) overlayFont.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
