package io.github.goldengoose.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.shared.Entity;
import io.github.goldengoose.shared.InputManager;

public class Player extends Entity {
    private static final float SPEED = 50f;

    private final Vector2 velocity = new Vector2();

    public Player(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void update(float deltaTime) {
        velocity.set(0f, 0f);
        velocity.y = InputManager.getVertical();
        velocity.x = InputManager.getHorizontal();
        position.mulAdd(velocity, SPEED * deltaTime);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y);
    }

    @Override
    public float getWidth() {
        return texture.getWidth();
    }

    @Override
    public float getHeight() {
        return texture.getHeight();
    }
}
