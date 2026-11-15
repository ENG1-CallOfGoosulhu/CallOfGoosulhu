package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.lighting.GameLighting;
import io.github.goldengoose.player.Player;

/**
 * UR_FIND_KEY: "The player finds a key that allows them to escape to the portal."
 *
 * The keycard lying on the campus map. Walking over it picks it up as an unmagnetised keycard.
 */
public class KeyCardItem extends Entity {
    private static final Color GLOW = new Color(0.6f, 0.9f, 1f, 1f);

    private final GameSession session;
    private final Rectangle area;
    private final Rectangle playerBounds = new Rectangle();
    private boolean collected;
    private float clock;

    public KeyCardItem(GameSession session, Rectangle area, Texture texture) {
        this.session = session;
        this.area = area;
        this.texture = texture;
        setPosition(area.x + (area.width - texture.getWidth()) / 2f,
            area.y + (area.height - texture.getHeight()) / 2f);
    }

    @Override
    public void update(float deltaTime) {
        clock += deltaTime;
    }

    @Override
    public void render(SpriteBatch batch) {
        float bob = Math.round(MathUtils.sin(clock * 3f) * 2f);
        batch.draw(texture, position.x, position.y + bob);
    }

    @Override
    public void interact(Entity other) {
        if (collected || !(other instanceof Player player)) return;

        Vector2 p = player.getPosition();
        playerBounds.set(p.x, p.y, player.getWidth(), player.getHeight());
        if (playerBounds.overlaps(area)) {
            collected = true;
            session.setKeyState(KeyState.UNMAGNETISED);
            session.getAudio().play(Assets.SFX_KEYCARD_PICKUP, 0.9f, 0.05f);
        }
    }

    @Override
    public void emitLight(GameLighting lighting) {
        float pulse = 0.5f + 0.5f * MathUtils.sin(clock * 3f);
        lighting.addLight(position.x + getWidth() / 2f, position.y + getHeight() / 2f,
            36f + 8f * pulse, GLOW, 0.6f + 0.3f * pulse);
    }

    @Override public float getSortY() { return area.y; }
    @Override public boolean isExpired() { return collected; }
    @Override public float getWidth()    { return texture.getWidth(); }
    @Override public float getHeight()   { return texture.getHeight(); }
}
