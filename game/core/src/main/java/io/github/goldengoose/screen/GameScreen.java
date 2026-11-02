package io.github.goldengoose.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.shared.*;
import io.github.goldengoose.spells.PentagramTrapSpell;
import io.github.goldengoose.spells.SpellTome;
import io.github.goldengoose.spells.TestSpell;

public class GameScreen implements Screen {
    private final Cog game;
    private final Viewport worldViewport = new FitViewport(400, 300);
    private final Viewport hudViewport = new ScreenViewport();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final InputMultiplexer multiplexer = new InputMultiplexer();

    private final Vector2 mouseWorldPosition = new Vector2();

    private final GameWorld world;
    private final GameTimer timer = new GameTimer(5 * 60);
    private final PauseMenu pauseMenu;
    private final SpellTome spellTome;
    private final BitmapFont hudFont = new BitmapFont();

    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer mapRenderer;

    private boolean paused = true;
    private boolean debug = false;

    public GameScreen(final Cog game) {
        this.game = game;
        world = new GameWorld(game.assets);

        pauseMenu = new PauseMenu(game);

        PentagramTrapSpell pentagramTrapSpell = new PentagramTrapSpell(world.getPlayer(), world, game.assets);
        TestSpell testSpell = new TestSpell(world.getPlayer(), game.assets);
        spellTome = new SpellTome(game.assets.getTexture(Assets.SPELL_TOME));
        spellTome.addSpell(pentagramTrapSpell);
        spellTome.addSpell(testSpell);

        tiledMap = new TmxMapLoader().load("maps/base_map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);


        multiplexer.addProcessor(pauseMenu.getStage());
        multiplexer.addProcessor(spellTome.getInputProcessor());
        multiplexer.addProcessor(gameInput);

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        if (InputManager.pause()) {
            paused = !paused;
            pauseMenu.setVisible(paused);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) debug = !debug;

        if (!paused) {
            world.update(delta);
            spellTome.update(delta);
            timer.update(delta);
        }

        Vector2 playerPos = world.getPlayer().getPosition();

        worldViewport.getCamera().position.set(playerPos.x, playerPos.y, 0f);
        worldViewport.apply();

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // World
        mapRenderer.setView((OrthographicCamera) worldViewport.getCamera());
        mapRenderer.render();
        game.batch.setProjectionMatrix(worldViewport.getCamera().combined);
        game.batch.begin();
        world.render(game.batch);
        game.batch.end();

        if (debug) renderDebug();

        // HUD
        hudViewport.apply();
        shapes.setProjectionMatrix(hudViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        spellTome.drawShapes(shapes, hudViewport);
        shapes.end();

        game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
        game.batch.begin();
        hudFont.draw(game.batch, String.format("%.1f", timer.getSecondsRemaining()),
            20, hudViewport.getWorldHeight() - 20);
        spellTome.drawSprites(game.batch, hudViewport);
        game.batch.end();

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

                spellTome.activateSelectedSpell(mouseWorldPosition);

                return true;
            }

            return false;
        }
    };

    private void renderDebug() {
        shapes.setProjectionMatrix(worldViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        world.renderDebug(shapes);
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
        tiledMap.dispose();
        mapRenderer.dispose();
        pauseMenu.dispose();
        hudFont.dispose();
    }

    // Unused overrides
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
