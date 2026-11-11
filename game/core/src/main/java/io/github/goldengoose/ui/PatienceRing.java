package io.github.goldengoose.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import io.github.goldengoose.assets.Assets;

/** Ring that empties as patience drops, with an eye that panics. (UR_PATIENCE) */
public class PatienceRing implements Disposable {

    public static final int SIZE = 64;
    public static final float CALM = 0.6f, UNEASY = 0.3f, PANIC = 0.1f;
    private static final float BLINK = 0.12f;

    private static final float RING_INNER = 13.2f, RING_OUTER = 18.6f;
    private static final float SHADOW = 14.2f, SHINE_INNER = 16.6f, SHINE_OUTER = 17.8f;

    private static final int DARK = 0, BASE = 1, LIGHT = 2, EDGE = 3;
    private static final Color[] GREEN = { Color.valueOf("245a2c"), Color.valueOf("46963e"), Color.valueOf("94e84f"), Color.valueOf("d6ffa0") };
    private static final Color[] GOLD = { Color.valueOf("786028"), Color.valueOf("b4963c"), Color.valueOf("e6ce6e"), Color.valueOf("faecaa") };
    private static final Color[] RED = { Color.valueOf("5a1222"), Color.valueOf("962234"), Color.valueOf("ce4048"), Color.valueOf("fa968c") };

    private final Texture frame;
    private final TextureRegion[][] eyes;
    private final Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
    private final Texture fillTexture = new Texture(pixmap);
    private final Color[] colours = { new Color(), new Color(), new Color(), new Color() };
    private float patience = 1f, clock, blinkTimer = 4f;

    public PatienceRing(Assets assets) {
        frame = assets.getTexture(Assets.PATIENCE_FRAME);
        eyes = TextureRegion.split(assets.getTexture(Assets.PATIENCE_EYES), SIZE * 2, SIZE * 2);
        update(0f, 1f);
    }

    /** patience goes from 1 (calm) to 0 (out of patience). (FR_PATIENCE) */
    public void update(float delta, float patience) {
        this.patience = patience;
        clock += delta;
        blinkTimer -= delta;
        if (blinkTimer < -BLINK) blinkTimer = MathUtils.random(3f, 7f);
        pickColours();

        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        float filled = patience * 360f;
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++) {
                float dx = x + 0.5f - SIZE / 2f, dy = y + 0.5f - SIZE / 2f;
                float radius = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) ((Math.toDegrees(Math.atan2(dx, -dy)) + 360) % 360);
                if (radius < RING_INNER || radius >= RING_OUTER || angle > filled) continue;

                Color colour = colours[BASE];
                if (radius < SHADOW) colour = colours[DARK];
                if (radius >= SHINE_INNER && radius < SHINE_OUTER) colour = colours[LIGHT];
                if (patience < 1f && filled - angle < 5f && radius >= 15f) colour = colours[EDGE];
                pixmap.drawPixel(x, y, Color.rgba8888(colour));
            }
        fillTexture.draw(pixmap, 0, 0);
    }

    public void draw(SpriteBatch batch, float x, float y, float pixel) {
        boolean blinking = blinkTimer < 0f && eyeState() < 3;
        batch.draw(frame, x, y, SIZE * pixel, SIZE * pixel);
        batch.draw(fillTexture, x, y, SIZE * pixel, SIZE * pixel);
        batch.draw(eyes[blinking ? 1 : 0][eyeState()], x, y, SIZE * pixel, SIZE * pixel);
    }

    private void pickColours() {
        Color[] palette = RED;
        if (patience > UNEASY) palette = GOLD;
        if (patience > CALM) palette = GREEN;
        float flash = patience <= UNEASY ? 0.25f + 0.25f * MathUtils.sin(clock * 6f) : 0f;
        for (int i = 0; i < 4; i++) colours[i].set(palette[i]).lerp(palette[EDGE], flash);
    }

    private int eyeState() {
        if (patience > CALM) return 0;
        if (patience > UNEASY) return 1;
        if (patience > PANIC) return 2;
        return 3;
    }

    @Override
    public void dispose() {
        fillTexture.dispose();
        pixmap.dispose();
    }
}
