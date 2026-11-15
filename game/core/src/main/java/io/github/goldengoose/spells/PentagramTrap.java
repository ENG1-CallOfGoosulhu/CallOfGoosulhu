package io.github.goldengoose.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.goose.Goose;
import io.github.goldengoose.lighting.GameLighting;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.world.RenderLayer;
import io.github.goldengoose.audio.AudioManager;
import io.github.goldengoose.assets.Assets;

/**
 * The glowing circle placed by the Pentagram spell. The first goose to walk into it is held in place
 * until it fades after LIFETIME seconds.
 */
public class PentagramTrap extends Entity {
    private static final int FRAME_SIZE = 48;
    private static final float GROW_FRAME_TIME = 0.07f;
    private static final float SHIMMER_FRAME_TIME = 0.12f;
    private static final float GROW_DURATION = GROW_FRAME_TIME * 6;
    private static final Color LIGHT_COLOUR = Color.RED;

    private static final float RADIUS = 24f;
    private static final float LIFETIME = 8f;

    private float lifetime = LIFETIME;
    private boolean triggered = false;
    private final AudioManager audio;

    static Animation<TextureRegion>[] rows;
    static Animation<TextureRegion> grow;
    static Animation<TextureRegion> shimmer;

    public PentagramTrap(Vector2 centre, Texture texture, AudioManager audio) {
        this.audio = audio;
        this.position.set(centre.x - RADIUS, centre.y - RADIUS);
        this.texture = texture;
        if (rows == null) {
            System.out.println("TEST, INNER");
            rows = Utilities.createAnimations(texture, GROW_FRAME_TIME, FRAME_SIZE);
            grow = rows[0];
            shimmer = rows[1];
            grow.setPlayMode(Animation.PlayMode.NORMAL);
            shimmer.setFrameDuration(SHIMMER_FRAME_TIME);
        }
        System.out.println("TEST, OUTER");
    }

    @Override
    public void update(float deltaTime) {
        lifetime -= deltaTime;
    }

    @Override
    public void render(SpriteBatch batch) {
        float size = RADIUS * 2f;

        TextureRegion frame;

        float age = LIFETIME - lifetime;
        if (age < GROW_DURATION) {
            frame = grow.getKeyFrame(age);
        } else if (lifetime < GROW_DURATION) {
            frame = grow.getKeyFrame(lifetime);
        } else {
            frame = shimmer.getKeyFrame(age - GROW_DURATION);
        }
        batch.draw(frame, position.x, position.y, FRAME_SIZE, FRAME_SIZE);
    }

    private final Vector2 centre = new Vector2();
    @Override
    public void emitLight(GameLighting lighting) {
        float age = LIFETIME - lifetime;

        float size = MathUtils.clamp(Math.min(age, lifetime) / GROW_DURATION, 0f, 1f);

        float pulse = 1f + 0.15f * MathUtils.sin((age - GROW_DURATION) / SHIMMER_FRAME_TIME * MathUtils.PI2);

        getCentre(centre);
        lighting.addLight(centre.x, centre.y, RADIUS * size * 2.5f, LIGHT_COLOUR, 0.9f * size * pulse);
    }

    @Override
    public float getWidth() {
        return RADIUS * 2;
    }

    @Override
    public float getHeight() {
        return RADIUS * 2;
    }

    @Override
    public int getRenderLayer() {
        return RenderLayer.GROUND;
    }

    public void checkGoose(Goose goose) {
        if (triggered || lifetime <= 0f) return;

        if (position.dst2(goose.getPosition()) <= RADIUS * RADIUS) {
            goose.getPosition().set(
                position.x - goose.getWidth() / 2f,
                position.y - goose.getHeight() / 2f
            );
            goose.trap(lifetime);
            triggered = true;
            playTrapSound();
        }

    }

    @Override
    public boolean isExpired() {
        return lifetime <= 0f;
    }

    @Override
    public void interact(Entity other) {
        if (!(other instanceof Goose goose) || triggered || isExpired()) return;

        Vector2 trapCenter = getCentre(new Vector2());
        Vector2 gooseCenter = goose.getCentre(new Vector2());

        if (trapCenter.dst2(gooseCenter) <= RADIUS * RADIUS) {
            goose.setPosition(trapCenter.x - goose.getWidth() / 2f, trapCenter.y - goose.getHeight() / 2f);
            goose.trap(lifetime);
            triggered = true;
            playTrapSound();
        }
    }

    private void playTrapSound(){
        if (audio == null) return;
        audio.playAt(Assets.SFX_PENTAGRAM_TRIGGER, position.x + RADIUS, position.y + RADIUS);
    }

    public boolean isTriggered() {
        return triggered;
    }

    public float getLifetime() {
        return lifetime;
    }
}
