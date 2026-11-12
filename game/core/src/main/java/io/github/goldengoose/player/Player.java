package io.github.goldengoose.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.util.InputManager;

public class Player extends Entity {
    private static final float SPEED = 120f;
    private static final float MUD_SPEED = 40f;

    private final Vector2 velocity = new Vector2();
    private TextureRegion sprite;
    private int hidingCount = 0;
    private boolean transformed = false;
    private boolean inMud = false;

    public Player(Texture texture) {
        this.texture = texture;
        sprite = new TextureRegion(texture, 96, 0, 32, 64);
    }

    public void transformIntoGoose(Texture gooseTexture) {
        if (!transformed) {
            transformed = true;
            sprite = new TextureRegion(gooseTexture, 0, 0, 32, 32);
        }
    }

    public boolean isTransformed() {
        return transformed;
    }

    public void setInMud(boolean inMud) {
        this.inMud = inMud;
    }

    @Override
    public void update(float deltaTime) {
        float speed = inMud ? MUD_SPEED : SPEED;
        velocity.set(InputManager.getHorizontal(), InputManager.getVertical()).nor();
        position.mulAdd(velocity, speed * deltaTime);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(sprite, position.x, position.y);
    }

    public void beginHiding() {
        hidingCount++;
    }

    public void endHiding() {
        if (hidingCount > 0) {
            hidingCount--;
        }
    }

    public boolean isHidden() {
        return hidingCount > 0;
    }

    @Override
    public float getWidth() {
        return sprite.getRegionWidth();
    }

    @Override
    public float getHeight() {
        return sprite.getRegionHeight();
    }
}
