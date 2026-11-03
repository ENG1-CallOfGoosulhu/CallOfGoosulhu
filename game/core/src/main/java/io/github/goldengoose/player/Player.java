package io.github.goldengoose.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.shared.Entity;
import io.github.goldengoose.shared.InputManager;

public class Player extends Entity {
    private static final float SPEED = 120f;

    private final Vector2 velocity = new Vector2();

    /** The student facing forwards, from the 32x32 character sheet. (UR_AVATAR) */
    private final TextureRegion sprite;

    public Player() {
        // The sheet has 4 poses (right, up, left, down), each 32 wide and 64 tall. We use "down".
        texture = new Texture("Characters/idle_32x32_2.png");
        sprite = new TextureRegion(texture, 96, 0, 32, 64);
    }

    @Override
    public void gameStart() {
    }

    @Override
    public void update(float deltaTime) {
        velocity.set(0f, 0f);
        velocity.y = InputManager.getVertical();
        velocity.x = InputManager.getHorizontal();

        position.add(velocity.scl(SPEED*deltaTime));
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(sprite, position.x, position.y);
    }
}
