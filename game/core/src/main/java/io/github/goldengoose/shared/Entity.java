package io.github.goldengoose.shared;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Abstract class for entities, basically anything with an in-game position and a texture
 */
public abstract class Entity {
    protected final Vector2 position = new Vector2();
    protected Texture texture;

    //----------------
    // Game loop
    //----------------

    /**
     * Called every frame, use for updating logic
     * @param deltaTime The time in seconds since the last frame. Used so framerate doesn't impact movement speed for example
     */
    public abstract void update(float deltaTime);

    /**
     * Called every frame, use for drawing textures
     * @param batch Used for drawing things on the screen
     */
    public abstract void render(SpriteBatch batch);

    public abstract float getWidth();
    public abstract float getHeight();

    public int getRenderLayer() {
        return RenderLayer.ENTITIES;
    }

    /** Return true to have the world remove this entity. */
    public boolean isExpired() {
        return false;
    }

    /** Called once per frame for every other entity in the world. */
    public void interact(Entity other) {}

    //----------------
    // Position
    //----------------

    /**
     * Returns the world position of the entity
     * @return The position of the entity as a Vector2
     */
    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getCentre(Vector2 out) {
        return out.set(position.x + getWidth() / 2f, position.y + getHeight() / 2f);
    }

    /**
     * Sets the world position of the entity
     */
    public void setPosition(float x, float y) {
        position.set(x, y);
    }
}
