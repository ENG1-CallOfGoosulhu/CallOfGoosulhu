package io.github.goldengoose.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.assets.Assets;

/**
 * Black fade with zooming, shaking tentacles drawn over the whole screen. Used by both cutscenes and
 * the zoom at the end of the game.
 */
public class TentacleFade {
    private static final float SHAKE = 6f;
    private static final float START_ZOOM = 1.8f;
    private static final float END_ZOOM = 1.05f;

    private final TextureRegion tentacles;
    private final ShapeRenderer shapes = new ShapeRenderer();

    public TentacleFade(Assets assets) {
        tentacles = new TextureRegion(assets.getTexture(Assets.VIGNETTE_2));
    }

    public void draw(SpriteBatch batch, Viewport viewport, float amount) {
        if (amount <= 0f) return;

        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, Math.min(amount * 2f, 1f));
        shapes.rect(0, 0, screenWidth, screenHeight);
        shapes.end();

        float zoom = MathUtils.lerp(START_ZOOM, END_ZOOM, amount);
        float width = screenWidth * zoom;
        float height = screenHeight * zoom;
        float shake = SHAKE * amount;
        float x = (screenWidth - width) / 2f + MathUtils.random(-shake, shake);
        float y = (screenHeight - height) / 2f + MathUtils.random(-shake, shake);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, amount);
        batch.draw(tentacles, x, y, width, height);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    public void dispose() {
        shapes.dispose();
    }
}
