package io.github.goldengoose.spells;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.world.GameSession;

/**
 * Base class for every spell in the tome. Checks the range and cooldown and plays the cast or fail
 * sound.
 */
public abstract class Spell {
    protected Texture texture;
    protected float range;
    protected float cooldown;

    protected final GameSession session;
    protected Assets assets;

    protected String name = "";
    protected String description = "";
    protected String castSound;

    public Spell(GameSession session, Assets assets) {
        this.session = session;
        this.assets = assets;
    }

    private float cooldownCounter;

    /**
     * Called when spell used
     * @param worldPos world position of the mouse when clicked
     * @return returns true if spell cast succeeded
     */
    public boolean activate(Vector2 worldPos) {
        if (cooldownCounter > 0 ||
            session.getPlayer().getPosition().dst2(worldPos) > range * range) {
            session.getAudio().play(Assets.SFX_SPELL_FAIL, 0.6f, 0f);
            return false;
        }

        cooldownCounter = cooldown;
        triggerEffect(worldPos);
        if (castSound != null) {
            session.getAudio().play(castSound, 1f, 0.05f);
        }
        return true;
    }

    /**
     * Called to trigger actual effect of spell
     * @param worldPos world position of the mouse when clicked
     */
    protected abstract void triggerEffect(Vector2 worldPos);

    /**
     * Reduce cooldown if spell triggered recently
     * @param deltaTime The time in seconds since the last frame
     */
    public void reduceCooldown(float deltaTime) {
        if (cooldownCounter <= 0) return;

        cooldownCounter -= deltaTime;
    }

    public Texture getTexture() {
        return texture;
    }
    public String getName() { return name; }
    public String getDescription() { return description; }


    public float getCooldownPercent() {
        if (cooldown <= 0) return 0f;
        return Math.max(0f, cooldownCounter / cooldown);
    }
}
