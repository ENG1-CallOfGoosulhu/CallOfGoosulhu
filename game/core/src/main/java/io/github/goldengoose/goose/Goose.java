package io.github.goldengoose.goose;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.shared.Assets;
import io.github.goldengoose.shared.Entity;
import io.github.goldengoose.shared.Utilities;

public class Goose extends Entity {
    //------------
    // CONSTANTS
    //------------
    private static final int FRAME_SIZE = 32;
    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float RUN_FRAME_TIME = 0.08f;

    private static final int ROW_FRONT = 0;
    private static final int ROW_BACK = 1;
    private static final int ROW_SIDE = 2;
    //------------
    // END CONSTANTS
    //------------

    private final GooseAI gooseAI = new GooseAI(this);
    private final Player player;

    private boolean honk;
    private float trappedTimer = 0f;

    private final Animation<TextureRegion>[] walkAnimations;
    private final Animation<TextureRegion>[] runAnimations;

    private float stateTime = 0f;

    public Goose(Player player, Vector2 position, Assets assets) {
        walkAnimations = Utilities.createAnimations(assets.getTexture(Assets.GOOSE_WALK), WALK_FRAME_TIME, FRAME_SIZE);
        runAnimations = Utilities.createAnimations(assets.getTexture(Assets.GOOSE_WALK), RUN_FRAME_TIME, FRAME_SIZE);

        this.player = player;
        this.position.set(position);
    }

    @Override
    public void gameStart() {
    }

    @Override
    public void update(float deltaTime) {
        if (trappedTimer > 0f) {
            trappedTimer -= deltaTime;
            return;
        }

        gooseAI.update(deltaTime);


        if (gooseAI.isMoving()) {
            stateTime += deltaTime;
        } else {
            stateTime = 0f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Vector2 facing = gooseAI.getFacingDirection();
        boolean flip = false;
        int row;

        if (Math.abs(facing.x) > Math.abs(facing.y)) {
            row = ROW_SIDE;
            flip = facing.x < 0f;
        } else if (facing.y > 0f) {
            row = ROW_BACK;
        } else {
            row = ROW_FRONT;
        }

        Animation<TextureRegion>[] animations = gooseAI.isChasing() ? runAnimations : walkAnimations;
        TextureRegion frame = animations[row].getKeyFrame(stateTime);

        if (flip) {
            batch.draw(frame, position.x + FRAME_SIZE, position.y, -FRAME_SIZE, FRAME_SIZE);
        } else {
            batch.draw(frame, position.x, position.y, FRAME_SIZE, FRAME_SIZE);
        }
    }

    public void renderDebug(ShapeRenderer shapes) {
        gooseAI.renderDebug(shapes);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public float getWidth() {
        return FRAME_SIZE;
    }

    @Override
    public float getHeight() {
        return FRAME_SIZE;
    }

    public void trap(float duration) {
        trappedTimer = Math.max(trappedTimer, duration);
    }

    public boolean isTrapped() {
        return trappedTimer > 0f;
    }
}
