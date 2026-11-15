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
import io.github.goldengoose.ui.TentacleFade;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.world.DepthRenderer;

import java.util.Arrays;
import java.util.Collections;

/**
 * Cutscene that plays when the game ends, win or lose. The tentacles fade, the player wakes up by the
 * lake, speech bubbles pop up, then they walk home before the game over screen. Space skips it.
 */
public class EndCutsceneScreen implements Screen {
    private static final float WAKE_TIME = 2f;
    private static final float STAND_START = 3f;
    private static final float FIRST_LINE_START = 3.5f;
    private static final float SECOND_LINE_START = 6f;
    private static final float WALK_START = 8.5f;
    private static final float FADE_START = 12f;
    private static final float END = 13f;

    private static final int SCALE = 2;
    private static final float BUBBLE_SCALE = 2f;
    private static final float BUBBLE_POP_TIME = 0.15f;
    private static final float BUBBLE_GAP = 4f;
    private static final float BUBBLE_TAIL_X = 8f;

    private final Cog game;
    private final boolean won;
    private final Viewport worldViewport = new FitViewport(600, 450);
    private final Viewport hudViewport = new ScreenViewport();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont skipFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, 24);
    private final Rectangle viewRect = new Rectangle();

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final DepthRenderer depthRenderer;
    private final TentacleFade tentacleFade;

    private final Vector2 wakeSpot;
    private final Vector2 walkHome;

    private final Animation<TextureRegion> sit;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> walkUp;
    private final TextureRegion whatBubble;
    private final TextureRegion coffeeBubble;

    private final Vector2 playerPos = new Vector2();
    private float time = 0f;
    private boolean finished = false;

    public EndCutsceneScreen(final Cog game, boolean won) {
        this.game = game;
        this.won = won;

        map = new TmxMapLoader().load("maps/Outside_campus_normal.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        depthRenderer = new DepthRenderer(map);
        tentacleFade = new TentacleFade(game.assets);

        wakeSpot = getPoint("wake_spot");
        walkHome = getPoint("walk_home");

        TextureRegion[][] amelia = TextureRegion.split(game.assets.getTexture(Assets.PLAYER), 16, 32);
        TextureRegion[][] ameliaSitting = TextureRegion.split(game.assets.getTexture(Assets.PLAYER), 32, 32);
        sit = new Animation<>(0.15f, Arrays.copyOf(ameliaSitting[4], 6));
        idle = new Animation<>(0.15f, Arrays.copyOf(amelia[1], 6));
        walkUp = new Animation<>(0.1f, Arrays.copyOfRange(amelia[2], 6, 12));

        whatBubble = new TextureRegion(game.assets.getTexture(Assets.BUBBLE_WHAT));
        coffeeBubble = new TextureRegion(game.assets.getTexture(Assets.BUBBLE_COFFEE));
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
            game.changeScreen(new GameOverScreen(game, won));
        }

        float waking = progress(0f, WAKE_TIME);
        float walking = progress(WALK_START, FADE_START);
        float fading = progress(FADE_START, END);

        playerPos.set(wakeSpot).lerp(walkHome, walking);

        OrthographicCamera camera = (OrthographicCamera) worldViewport.getCamera();
        camera.position.set(wakeSpot.x, Math.max(wakeSpot.y, camera.viewportHeight / 2f), 0f);
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
        drawPlayer(batch);
        drawBubble(batch);
        batch.end();

        drawOverlay(batch, waking, fading);
    }

    private void drawPlayer(SpriteBatch batch) {
        TextureRegion frame;
        if (time < STAND_START) {
            frame = sit.getKeyFrame(time, true);
        } else if (time < WALK_START) {
            frame = idle.getKeyFrame(time, true);
        } else {
            frame = walkUp.getKeyFrame(time, true);
        }

        float width = frame.getRegionWidth() * SCALE;
        float height = frame.getRegionHeight() * SCALE;
        batch.draw(frame, playerPos.x - width / 2f, playerPos.y, width, height);
    }

    private void drawBubble(SpriteBatch batch) {
        TextureRegion bubble;
        float shownFor;
        if (time >= FIRST_LINE_START && time < SECOND_LINE_START) {
            bubble = whatBubble;
            shownFor = time - FIRST_LINE_START;
        } else if (time >= SECOND_LINE_START && time < WALK_START) {
            bubble = coffeeBubble;
            shownFor = time - SECOND_LINE_START;
        } else {
            return;
        }

        float pop = Math.min(shownFor / BUBBLE_POP_TIME, 1f);
        float width = bubble.getRegionWidth() * BUBBLE_SCALE * pop;
        float height = bubble.getRegionHeight() * BUBBLE_SCALE * pop;
        float x = playerPos.x - BUBBLE_TAIL_X;
        float y = playerPos.y + 32 * SCALE + BUBBLE_GAP;
        batch.draw(bubble, x, y, width, height);
    }

    private void drawOverlay(SpriteBatch batch, float waking, float fading) {
        hudViewport.apply();
        tentacleFade.draw(batch, hudViewport, 1f - waking);

        if (fading > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.setProjectionMatrix(hudViewport.getCamera().combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, fading);
            shapes.rect(0, 0, hudViewport.getWorldWidth(), hudViewport.getWorldHeight());
            shapes.end();
        }

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        skipFont.draw(batch, "SPACE - Skip", 0, 40, hudViewport.getWorldWidth() - 20, Align.right, false);
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
        tentacleFade.dispose();
        shapes.dispose();
        skipFont.dispose();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
