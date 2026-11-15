package io.github.goldengoose.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.assets.CogFonts;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.ui.Hud;
import io.github.goldengoose.ui.PauseMenu;
import io.github.goldengoose.ui.TentacleFade;
import io.github.goldengoose.ui.TomeMenu;
import io.github.goldengoose.util.InputManager;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.world.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.Color;

/**
 * UR_WIN: "The player must get to the escape portal by dispersing the obstructing geese, before the timer ends, to win." (See portal)
 * FR_TIMER_COUNTDOWN: "The system shall end the game once timer is up"
 *
 * The main gameplay screen. Updates and draws the current map, HUD, pause menu and spell tome, and
 * runs the ending: time up or patience gone or portal end, then the zoom, tentacles and end cutscene.
 */
public class GameScreen implements Screen {
    private static final float ENDING_LENGTH = 3f;
    private static final float ENDING_ZOOM = 0.25f;
    private static final float PULL_LENGTH = 1.5f;

    private final Cog game;
    private final Viewport worldViewport = new FitViewport(600, 450);
    private final Viewport hudViewport = new ScreenViewport();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final InputMultiplexer multiplexer = new InputMultiplexer();

    private final Vector2 mouseWorldPosition = new Vector2();
    private final Vector2 playerPos = new Vector2();
    private final Vector2 pullStart = new Vector2();
    private final Vector2 pullEnd = new Vector2();
    private final Rectangle playerFeet = new Rectangle();

    private final PauseMenu pauseMenu;
    private final TomeMenu tomeMenu;
    private final BitmapFont hudFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, 24);
    private final BitmapFont objectiveFont = Utilities.smooth(Utilities.createFont(CogFonts.UNCIALANTIQUA, 40, 2f, Color.BLACK, Color.WHITE));
    private final Hud hud;
    private final Rectangle viewRect = new Rectangle();
    private final GameSession session;
    private final TentacleFade tentacleFade;

    private boolean paused = true;
    private boolean tomeOpen = false;
    private boolean debug = false;
    private boolean gameOver = false;
    private float gameOverTimer = -1f;
    private boolean gameOverWon = false;
    private float endingTime = -1f;
    private float pullTime = -1f;

    public GameScreen(final Cog game) {
        this.game = game;
        session = new GameSession(game.assets, game.audio, worldViewport, "Outside_campus_cthulhu_props", "start");
        pauseMenu = new PauseMenu(game);
        tomeMenu = new TomeMenu(game, game.assets.getTexture(Assets.SPELL_TOME_EMPTY), session.getSpellTome());
        hud = new Hud(game.assets, 5 * 60);
        tentacleFade = new TentacleFade(game.assets);
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3))  {
            debug = !debug;
            session.setDebug(debug);
        }

        if (!paused) {
            session.update(delta);
            hud.update(delta, session.getTimer().getSecondsRemaining());
            hud.updatePatience(session.getPatienceSystem().getPatience());

            if (session.getTimer().isTimeUp() && !gameOver) {
                gameOver = true;
                session.getPlayer().transformIntoGoose(game.assets.getTexture(Assets.GOOSE_WALK));
                gameOverTimer = 2f;
                gameOverWon = false;
            }
            if (session.getPatienceSystem().isTransformed() && !gameOver) {
                gameOver = true;
                session.getPlayer().transformIntoGoose(game.assets.getTexture(Assets.GOOSE_WALK));
                gameOverTimer = 2f;
                gameOverWon = false;
            }
            Player player = session.getPlayer();
            Rectangle portal = session.getArea().getPortal();
            if (portal != null && !gameOver && portal.overlaps(player.getHitbox(playerFeet))) {
                gameOver = true;
                gameOverWon = true;
                pullTime = 0f;
                pullStart.set(player.getPosition());
                portal.getCenter(pullEnd);
                pullEnd.sub(player.getWidth() / 2f, player.getHeight() / 2f);
            }
            if (pullTime >= 0f) {
                pullTime += delta;
                float pulling = Math.min(pullTime / PULL_LENGTH, 1f);
                player.getPosition().set(pullStart).lerp(pullEnd, pulling);
                player.setPulling(pulling);
                if (pulling >= 1f && endingTime < 0f) {
                    endingTime = 0f;
                }
            }
            if (gameOverTimer > 0) {
                gameOverTimer -= delta;
                if (gameOverTimer <= 0) {
                    endingTime = 0f;
                }
            }
            if (endingTime >= 0f) {
                endingTime += delta;
                if (endingTime >= ENDING_LENGTH) {
                    game.changeScreen(new EndCutsceneScreen(game, gameOverWon));
                }
            }
        }

        float ending = 0f;
        if (endingTime >= 0f) {
            ending = Math.min(endingTime / ENDING_LENGTH, 1f);
        }

        session.getPlayer().getCentre(playerPos);
        OrthographicCamera camera = (OrthographicCamera) worldViewport.getCamera();
        camera.position.set(playerPos.x, playerPos.y, 0f);
        camera.zoom = MathUtils.lerp(1f, ENDING_ZOOM, ending);
        worldViewport.apply();

        viewRect.set(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth, camera.viewportHeight);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        session.getArea().render(game.batch, camera, viewRect);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        session.draw(game.batch);
        game.batch.end();

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

        if (paused) {
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

        float tentacles = Math.max(ending * 2f - 1f, 0f);
        hudViewport.apply();
        tentacleFade.draw(game.batch, hudViewport, tentacles);
    }

    private void drawObjective() {
        ObjectiveSystem obj = session.getObjectiveSystem();
        if (!obj.isVisible()) return;

        float w = hudViewport.getWorldWidth();
        float h = hudViewport.getWorldHeight();
        float textWidth = w * 0.55f;

        objectiveFont.getData().setScale(h / Gdx.graphics.getDisplayMode().height);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
        game.batch.begin();
        objectiveFont.setColor(1f, 1f, 1f, obj.getAlpha());
        objectiveFont.draw(game.batch, obj.getCurrentObjective(),
            (w - textWidth) / 2f, h * 0.95f, textWidth, Align.center, true);
        objectiveFont.setColor(Color.WHITE);
        game.batch.end();
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
        tentacleFade.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
