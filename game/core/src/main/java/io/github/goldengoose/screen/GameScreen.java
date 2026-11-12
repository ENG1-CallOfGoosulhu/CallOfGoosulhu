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
import io.github.goldengoose.ui.Hud;
import io.github.goldengoose.ui.PauseMenu;
import io.github.goldengoose.ui.TomeMenu;
import io.github.goldengoose.util.InputManager;
import io.github.goldengoose.world.*;
import com.badlogic.gdx.math.Rectangle;

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
    private final BitmapFont hudFont = new BitmapFont();
    private final Hud hud;

    private final Rectangle viewRect = new Rectangle();

    private final GameSession session;

    private boolean paused = true;
    private boolean tomeOpen = false;
    private boolean debug = false;

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
        // Input
        if (!tomeOpen && InputManager.pause()) {
            paused = !paused;
            pauseMenu.setVisible(paused);
        }
        if ((paused == tomeOpen) && InputManager.tome()) {
            tomeOpen = !tomeOpen;
            paused = tomeOpen;
            session.getSpellTome().setVisible(!tomeOpen);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) debug = !debug;

        // Update
        if (!paused) {
            session.update(delta);
            hud.update(delta, session.getTimer().getSecondsRemaining());
        }

        // Camera follows the player
        session.getPlayer().getCentre(playerPos);
        OrthographicCamera camera = (OrthographicCamera) worldViewport.getCamera();
        camera.position.set(playerPos.x, playerPos.y, 0f);
        worldViewport.apply();

        viewRect.set(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth, camera.viewportHeight);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // World
        session.getArea().render(game.batch, camera, viewRect);

        if (debug) renderDebug();

        // HUD
        hudViewport.apply();
        hud.draw(game.batch, hudViewport, hudFont, session.getSpellTome(),session.getKeyState());

        shapes.setProjectionMatrix(hudViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        session.getSpellTome().drawShapes(shapes, hudViewport);
        shapes.end();

        game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
        game.batch.begin();
        session.getSpellTome().drawSprites(game.batch, hudViewport);
        game.batch.end();

        // Menus
        if (paused) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.setProjectionMatrix(hudViewport.getCamera().combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.5f);
            shapes.rect(0, 0, hudViewport.getWorldWidth(), hudViewport.getWorldHeight());
            shapes.end();

            pauseMenu.act(delta);
            pauseMenu.draw();
        }
        if (tomeOpen) {
            game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
            game.batch.begin();
            tomeMenu.draw(game.batch, hudViewport);
            game.batch.end();
        }

        //TODO: Remove once game has proper exit feature, this is just for testing
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private final InputAdapter gameInput = new InputAdapter() {
        @Override
        public boolean touchDown(int screenX, int screenY,
                                 int pointer, int button) {

            if (paused) return false;

            if (button == Input.Buttons.LEFT) {
                worldViewport.unproject(
                    mouseWorldPosition.set(screenX, screenY)
                );

                session.getSpellTome().activateSelectedSpell(mouseWorldPosition);

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
        hud.dispose();
    }

    // Unused overrides
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
