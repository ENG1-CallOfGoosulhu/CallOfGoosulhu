package io.github.goldengoose.spells;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.assets.Assets;

public abstract class Spell {
    protected Texture texture;
    protected float range;
    protected float cooldown;

    protected Player player;
    protected Assets assets;

    public Spell(Player player, Assets assets) {
        this.player = player;
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
            player.getPosition().dst2(worldPos) > range * range) {
            return false;
        }

        cooldownCounter = cooldown;
        triggerEffect(worldPos);
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

    public float getCooldownPercent() {
        if (cooldown <= 0) return 0f;
        return Math.max(0f, cooldownCounter / cooldown);
    }
}
