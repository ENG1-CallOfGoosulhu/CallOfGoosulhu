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

public class ElderSign extends Entity {
    private static final int FRAME_SIZE = 48;
    private static final float GROW_FRAME_TIME = 0.07f;
    private static final float SHIMMER_FRAME_TIME = 0.12f;
    private static final float GROW_DURATION = GROW_FRAME_TIME * 6;
    private static final Color LIGHT_COLOUR = Color.YELLOW;


    private static final float RADIUS = 24f;
    private static final float LIFETIME = 8f;
    private static final float SCARE_RADIUS = 300f;

    private final Vector2 otherCentre = new Vector2();

    private float lifetime = LIFETIME;
    private boolean triggered = false;

    static Animation<TextureRegion>[] rows;
    static Animation<TextureRegion> grow;
    static Animation<TextureRegion> shimmer;

    public ElderSign(Vector2 centre, Texture texture) {
        this.position.set(centre.x - RADIUS, centre.y - RADIUS);
        this.texture = texture;
        if (rows == null) {
            rows = Utilities.createAnimations(texture, GROW_FRAME_TIME, FRAME_SIZE);
            grow = rows[0];
            shimmer = rows[1];
            grow.setPlayMode(Animation.PlayMode.NORMAL);
            shimmer.setFrameDuration(SHIMMER_FRAME_TIME);
        }
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

    @Override
    public boolean isExpired() {
        return lifetime <= 0f;
    }

    @Override
    public void interact(Entity other) {
        if (isExpired() || !(other instanceof Goose goose)) return;

        getCentre(centre);
        goose.getCentre(otherCentre);
        if (centre.dst2(otherCentre) <= SCARE_RADIUS * SCARE_RADIUS) {
            goose.scareFrom(centre);
        }
    }

    public boolean isTriggered() {
        return triggered;
    }

    public float getLifetime() {
        return lifetime;
    }
}
