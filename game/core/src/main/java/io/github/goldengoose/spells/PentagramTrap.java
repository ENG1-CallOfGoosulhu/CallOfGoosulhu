package io.github.goldengoose.spells;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.goose.Goose;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.world.RenderLayer;

public class PentagramTrap extends Entity {

    private static final float RADIUS = 15f;
    private static final float LIFETIME = 8f;

    private float lifetime = LIFETIME;
    private boolean triggered = false;

    public PentagramTrap(Vector2 centre, Texture texture) {
        this.position.set(centre.x - RADIUS, centre.y - RADIUS);
        this.texture = texture;
    }

    @Override
    public void update(float deltaTime) {
        lifetime -= deltaTime;
    }

    @Override
    public void render(SpriteBatch batch) {
        float size = RADIUS * 2f;

        batch.draw(texture, position.x, position.y, size, size);
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
        }
    }

    public boolean isTriggered() {
        return triggered;
    }

    public float getLifetime() {
        return lifetime;
    }
}
