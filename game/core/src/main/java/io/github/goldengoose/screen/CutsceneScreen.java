package io.github.goldengoose.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.assets.CogFonts;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.world.DepthRenderer;

import java.util.Arrays;
import java.util.Collections;

public class CutsceneScreen implements Screen {
    private static final float FEED_START = 4f;
    private static final float PORTAL_START = 6.5f;
    private static final float PULL_START = 8.5f;
    private static final float FADE_START = 10f;
    private static final float END = 12.5f;

    private static final int SCALE = 2;
    private static final int GOOSE_SIZE = 32;
    private static final int PORTAL_SIZE = 48;
    private static final float THROW_TIME = 1f;
    private static final float HAND_HEIGHT = 32f;
    private static final float BREAD_THROW_HEIGHT = 48f;
    private static final float PULL_SPIN = 720f;
    private static final float VIGNETTE_SHAKE = 6f;
    private static final float VIGNETTE_START_ZOOM = 1.8f;
    private static final float VIGNETTE_END_ZOOM = 1.05f;

    private final Cog game;
    private final Viewport worldViewport = new FitViewport(600, 450);
    private final Viewport hudViewport = new ScreenViewport();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont skipFont = Utilities.createFont(CogFonts.CRISTOFORO, 24);
    private final Rectangle viewRect = new Rectangle();

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final DepthRenderer depthRenderer;

    private final Vector2 playerStart;
    private final Vector2 throwSpot;
    private final Vector2 portal;
    private final Vector2[] gooseStarts;

    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> walk;
    private final Animation<TextureRegion> gooseBob;
    private final Animation<TextureRegion> portalOpen;
    private final Animation<TextureRegion> portalSwirl;
    private final TextureRegion bread;
    private final TextureRegion vignette;

    private final Vector2 playerPos = new Vector2();
    private final Vector2 goosePos = new Vector2();
    private float time = 0f;
    private boolean finished = false;

    public CutsceneScreen(final Cog game) {
        this.game = game;

        map = new TmxMapLoader().load("maps/Outside_campus_normal.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        depthRenderer = new DepthRenderer(map);

        playerStart = getPoint("player_start");
        throwSpot = getPoint("feed_spot");
        portal = getPoint("portal");
        gooseStarts = new Vector2[] {getPoint("goose_1"), getPoint("goose_2"), getPoint("goose_3")};

        TextureRegion[][] amelia = TextureRegion.split(game.assets.getTexture(Assets.PLAYER), 16, 32);
        idle = new Animation<>(0.15f, Arrays.copyOf(amelia[1], 6));
        walk = new Animation<>(0.1f, Arrays.copyOf(amelia[2], 6));

        TextureRegion[][] portalFrames = TextureRegion.split(game.assets.getTexture(Assets.PORTAL), PORTAL_SIZE, PORTAL_SIZE);
        float openFrameTime = (PULL_START - PORTAL_START) / 6f;
        portalOpen = new Animation<>(openFrameTime, portalFrames[0]);
        portalSwirl = new Animation<>(0.1f, portalFrames[1]);

        gooseBob = new Animation<>(0.4f, TextureRegion.split(game.assets.getTexture(Assets.GOOSE_NORMAL), GOOSE_SIZE, GOOSE_SIZE)[0]);
        bread = new TextureRegion(game.assets.getTexture(Assets.BREAD));
        vignette = new TextureRegion(game.assets.getTexture(Assets.VIGNETTE_2));
    }

    private Vector2 getPoint(String name) {
        MapProperties point = map.getLayers().get("Cutscene").getObjects().get(name).getProperties();
        return new Vector2(point.get("x", Float.class), point.get("y", Float.class));
    }

    private float progress(float start, float end) {
        return MathUtils.clamp((time - start) / (end - start), 0f, 1f);
    }

    @Override
    public void render(float delta) {
        time += delta;
        boolean skipPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        if ((time >= END || skipPressed) && !finished) {
            finished = true;
            game.changeScreen(new GameScreen(game));
        }

        float walking = progress(0f, FEED_START);
        float feeding = progress(FEED_START, PORTAL_START);
        float opening = progress(PORTAL_START, PULL_START);
        float pulling = progress(PULL_START, FADE_START);
        float fading = progress(FADE_START, END);

        if (pulling > 0f) {
            playerPos.set(throwSpot).lerp(portal, pulling);
        } else {
            playerPos.set(playerStart).lerp(throwSpot, walking);
        }

        OrthographicCamera camera = (OrthographicCamera) worldViewport.getCamera();
        camera.position.set(playerPos.x, Math.max(playerPos.y, camera.viewportHeight / 2f), 0f);
        worldViewport.apply();
        viewRect.set(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth, camera.viewportHeight);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        mapRenderer.setView(camera);
        mapRenderer.render(depthRenderer.getFlatLayers());

        SpriteBatch batch = game.batch;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        depthRenderer.render(batch, Collections.emptyList(), viewRect);
        drawPortal(batch, opening);
        drawBread(batch, opening);
        drawGeese(batch, feeding, opening, pulling);
        drawPlayer(batch, walking, pulling);
        batch.end();

        drawOverlay(batch, fading);
    }

    private void drawPortal(SpriteBatch batch, float opening) {
        if (opening <= 0f) return;

        TextureRegion frame;
        if (opening < 1f) {
            frame = portalOpen.getKeyFrame(time - PORTAL_START);
        } else {
            frame = portalSwirl.getKeyFrame(time, true);
        }

        float size = PORTAL_SIZE * SCALE;
        batch.draw(frame, portal.x - size / 2f, portal.y - size / 2f, size, size);
    }

    private void drawBread(SpriteBatch batch, float opening) {
        float thrown = progress(FEED_START, FEED_START + THROW_TIME);
        if (thrown <= 0f || opening >= 1f) return;

        float x = MathUtils.lerp(throwSpot.x, portal.x, thrown);
        float y = MathUtils.lerp(throwSpot.y + HAND_HEIGHT, portal.y, thrown) + MathUtils.sin(thrown * MathUtils.PI) * BREAD_THROW_HEIGHT;
        float size = bread.getRegionWidth() * (1f - opening);
        batch.draw(bread, x - size / 2f, y - size / 2f, size, size);
    }

    private void drawGeese(SpriteBatch batch, float feeding, float opening, float pulling) {
        float scale = 1f - pulling;
        if (scale <= 0f) return;

        TextureRegion frame = gooseBob.getKeyFrame(time, true);
        for (Vector2 start : gooseStarts) {
            Vector2 feedingSpot = new Vector2(start).lerp(portal, 0.5f);

            if (pulling > 0f) {
                goosePos.set(start).lerp(portal, pulling);
            } else if (opening > 0f) {
                goosePos.set(feedingSpot).lerp(start, opening);
            } else {
                goosePos.set(start).lerp(feedingSpot, feeding);
            }

            boolean facingRight = portal.x > start.x;
            if (opening > 0f) {
                facingRight = !facingRight;
            }

            float scaleX = scale;
            if (!facingRight) {
                scaleX = -scale;
            }

            batch.draw(frame, goosePos.x - GOOSE_SIZE / 2f, goosePos.y - GOOSE_SIZE / 2f, GOOSE_SIZE / 2f, GOOSE_SIZE / 2f,
                GOOSE_SIZE, GOOSE_SIZE, scaleX, scale, pulling * PULL_SPIN);
        }
    }

    private void drawPlayer(SpriteBatch batch, float walking, float pulling) {
        float scale = 1f - pulling;
        if (scale <= 0f) return;

        TextureRegion frame;
        if (walking < 1f) {
            frame = walk.getKeyFrame(time, true);
        } else {
            frame = idle.getKeyFrame(time, true);
        }

        float width = frame.getRegionWidth() * SCALE;
        float height = frame.getRegionHeight() * SCALE;
        batch.draw(frame, playerPos.x - width / 2f, playerPos.y, width / 2f, height / 2f,
            width, height, scale, scale, pulling * PULL_SPIN);
    }

    private void drawOverlay(SpriteBatch batch, float fading) {
        hudViewport.apply();
        float screenWidth = hudViewport.getWorldWidth();
        float screenHeight = hudViewport.getWorldHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(hudViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, Math.min(fading * 2f, 1f));
        shapes.rect(0, 0, screenWidth, screenHeight);
        shapes.end();

        float zoom = MathUtils.lerp(VIGNETTE_START_ZOOM, VIGNETTE_END_ZOOM, fading);
        float width = screenWidth * zoom;
        float height = screenHeight * zoom;
        float shake = VIGNETTE_SHAKE * fading;
        float x = (screenWidth - width) / 2f + MathUtils.random(-shake, shake);
        float y = (screenHeight - height) / 2f + MathUtils.random(-shake, shake);

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, fading);
        batch.draw(vignette, x, y, width, height);
        batch.setColor(1f, 1f, 1f, 1f);
        skipFont.draw(batch, "SPACE - Skip", 0, 40, screenWidth - 20, Align.right, false);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height);
        hudViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        shapes.dispose();
        skipFont.dispose();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
