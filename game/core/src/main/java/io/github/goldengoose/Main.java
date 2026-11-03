package io.github.goldengoose;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.lighting.GameLighting;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.shared.GameWorld;
import io.github.goldengoose.shared.GameTimer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    BitmapFont font;
    private final Viewport viewport = new FitViewport(
        600,
        450,
        new OrthographicCamera()
    );

    GameWorld world = new GameWorld();
    GameTimer timer = new GameTimer();
    Player player;

    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;

    /** Spooky night lighting. (UR_ALTER_CAMPUS) */
    GameLighting lighting;

    @Override
    public void create() {
        batch = new SpriteBatch();
        player = new Player();
        player.setPosition(23 * 32, 2 * 32);
        world.addEntity(player);
        world.init();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        tiledMap = new TmxMapLoader().load("maps/Outside_campus_cthulhu.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        lighting = new GameLighting(tiledMap, viewport);

        font = new BitmapFont();

    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        viewport.apply();
        var camera = (OrthographicCamera) viewport.getCamera();
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        float deltaTime = Gdx.graphics.getDeltaTime();
        timer.update(deltaTime);

        if (!timer.isPaused()) {
            world.update(deltaTime);
        }

        batch.begin();
        world.render(batch);
        batch.end();

        // Lighting goes over the map and player, but not the HUD. (UR_ALTER_CAMPUS)
        lighting.render();

        batch.begin();
        float x = camera.position.x - 290;
        float y = camera.position.y + 215;

        String timeLeft = String.format("%.1f", timer.getSecondsRemaining());

        font.draw(batch, timeLeft, x, y);
        font.draw(batch, "Press P to pause", x, y -20);

        batch.end();

        //TODO: Remove once game has proper exit feature, this is just for testing
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        lighting.dispose();
        image.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
