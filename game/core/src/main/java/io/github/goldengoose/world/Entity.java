package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.lighting.GameLighting;

/**
 * Abstract class for entities, basically anything with an in-game position and a texture
 */
public abstract class Entity {
    protected final Vector2 position = new Vector2();
    protected Texture texture;
    private final Rectangle hitbox = new Rectangle();

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

    /** Called every frame before lighting is drawn. Override to give off light. */
    public void emitLight(GameLighting lighting) {}

    /**
     * Returns the hitbox at its current position
     * @param out Rectangle to be set
     * @return Returns the modified out Rectangle
     */
    public Rectangle getHitbox(Rectangle out) {
        return out.set(position.x + getWidth() * 0.25f, position.y, getWidth() * 0.5f, 8f);
    }

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

    /**
     * Move relative to current position
     * @param dx x-axis movement
     * @param dy y-axis movement
     * @param collision Collision map
     */
    public void moveBy(float dx, float dy, CollisionMap collision) {
        if (collision == null) {
            position.add(dx, dy);
            return;
        }

        position.x += dx;
        if (collision.isBlocked(getHitbox(hitbox))) position.x -= dx;

        position.y += dy;
        if (collision.isBlocked(getHitbox(hitbox))) position.y -= dy;
    }

    /**
     * Where this entity touches the ground. If texture has space below, override and add as many pixels to the pos.y
     * @return the y coordinate of the bottom of the sprite
     */
    public float getSortY() {
        return position.y;
    }
}
