package io.github.goldengoose.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.spells.SpellTome;
import io.github.goldengoose.world.KeyState;

/** Hourglass timer, patience ring and vignette. (UR_GAME_TIMER, UR_PATIENCE) */
public class Hud implements Disposable {

    private static final float HUD_SCALE = 225f;

    private final float totalSeconds;
    private final Hourglass hourglass;
    private final PatienceRing ring;
    private final KeyCard keyCard;
    private final Texture[] vignettes;
    private float secondsLeft, patience = 1f, clock;

    public Hud(Assets assets, float totalSeconds) {
        this.totalSeconds = totalSeconds;
        secondsLeft = totalSeconds;
        hourglass = new Hourglass(assets);
        ring = new PatienceRing(assets);
        keyCard = new KeyCard(assets);
        vignettes = new Texture[] {
            assets.getTexture(Assets.VIGNETTE_0), assets.getTexture(Assets.VIGNETTE_1), assets.getTexture(Assets.VIGNETTE_2) };
    }

    /** Call every frame while the game isn't paused. (FR_TIMER_COUNTDOWN) */
    public void update(float delta, float secondsLeft) {
        this.secondsLeft = secondsLeft;
        clock += delta;
        hourglass.update(delta, 1f - secondsLeft / totalSeconds);
        ring.update(delta, patience);
    }

    /** 1 is calm, 0 is out of patience (FR_PATIENCE) */
    public void updatePatience(float patience) {
        this.patience = MathUtils.clamp(patience, 0f, 1f);
    }

    /** Draws the whole HUD on top of the game. (UR_GAME_TIMER, UR_PATIENCE) */
    public void draw(SpriteBatch batch, Viewport viewport, BitmapFont font, SpellTome tome, KeyState keyState) {
        float width = viewport.getWorldWidth(), height = viewport.getWorldHeight();
        float pixel = Math.max(2, Math.round(height / HUD_SCALE));
        float margin = 3 * pixel;

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawVignettes(batch, width, height);

        float glassY = height - margin - Hourglass.HEIGHT * pixel;
        hourglass.draw(batch, margin, glassY, pixel);
        int seconds = MathUtils.ceil(secondsLeft);

        if (secondsLeft <= 29f) {
            float pulse = 0.6f + 0.4f * MathUtils.sin(clock * 6f);
            font.setColor(0f, pulse, 0f, 1f);
        } else {
            font.setColor(Color.WHITE);
        }
        font.draw(batch, String.format("%d:%02d", seconds / 60, seconds % 60), margin, glassY - pixel, Hourglass.WIDTH * pixel, Align.center, false);
        font.setColor(Color.WHITE);
        float ringSize = PatienceRing.SIZE * pixel;
        ring.draw(batch, width - margin - ringSize, height - margin - ringSize, pixel);

        float cardPixel = tome.getPixel(viewport);
        float cardX = tome.getRight(viewport) - KeyCard.WIDTH * cardPixel;
        float cardY = tome.getTop(viewport) + 2 * cardPixel;   // 2 tome pixels of padding
        keyCard.draw(batch, keyState, cardX, cardY, cardPixel);
        batch.end();
    }

    private void drawVignettes(SpriteBatch batch, float width, float height) {
        float blink = 0.85f + 0.15f * MathUtils.sin(clock * 3f);
        batch.draw(vignettes[0], 0, 0, width, height);
        batch.setColor(1f, 1f, 1f, vignette(PatienceRing.CALM));
        batch.draw(vignettes[1], 0, 0, width, height);
        batch.setColor(1f, 1f, 1f, vignette(PatienceRing.UNEASY) * blink);
        batch.draw(vignettes[2], 0, 0, width, height);
        batch.setColor(Color.WHITE);
    }

    private float vignette(float level) {
        return MathUtils.clamp((level - patience) / 0.3f, 0f, 1f);
    }

    @Override
    public void dispose() {
        hourglass.dispose();
        ring.dispose();
    }
}
