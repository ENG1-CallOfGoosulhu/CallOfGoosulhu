package io.github.goldengoose.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.world.CollisionMap;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.util.InputManager;
import io.github.goldengoose.audio.AudioManager;
import io.github.goldengoose.assets.Assets;

import java.util.Arrays;

/**
 * The student the player controls. Walks in 8 directions and animates using the Amelia sheet. (UR_AVATAR, FR_MOVE_PLAYER)
 */
public class Player extends Entity {
    //------------
    // CONSTANTS
    //------------
    private static final float SPEED = 120f;
    private static final float MUD_SPEED = 60f;

    // Amelia frames are 16x32 on the sheet, drawn at double size to match our 32px tiles
    private static final int FRAME_WIDTH = 16;
    private static final int FRAME_HEIGHT = 32;
    private static final int DRAW_WIDTH = FRAME_WIDTH * 2;
    private static final int DRAW_HEIGHT = FRAME_HEIGHT * 2;

    // Rows on the sheet (row 0 is just still poses)
    private static final int IDLE_ROW = 1;
    private static final int WALK_ROW = 2;
    private static final int FRAMES_PER_DIRECTION = 6;
    private static final float IDLE_FRAME_TIME = 0.15f;
    private static final float WALK_FRAME_TIME = 0.1f;

    // Each row is 4 blocks of 6 frames, in this order
    private static final int FACING_RIGHT = 0;
    private static final int FACING_UP = 1;
    private static final int FACING_LEFT = 2;
    private static final int FACING_DOWN = 3;

    private static final int[] STEP_FRAMES = {1, 4};
    //------------
    // END CONSTANTS
    //------------

    private final Vector2 velocity = new Vector2();
    private final Animation<TextureRegion>[] idleAnimations;
    private final Animation<TextureRegion>[] walkAnimations;
    private float stateTime = 0f;
    private int facing = FACING_DOWN;
    private boolean moving = false;

    private TextureRegion gooseSprite;
    private int hidingCount = 0;
    private boolean transformed = false;
    private boolean inMud = false;

    private CollisionMap collision;
    private AudioManager audio;
    private int lastFrameIndex = -1;

    public Player(Texture texture) {
        this.texture = texture;
        idleAnimations = createRowAnimations(texture, IDLE_ROW, IDLE_FRAME_TIME);
        walkAnimations = createRowAnimations(texture, WALK_ROW, WALK_FRAME_TIME);
    }

    /**
     * Splits one row of the sheet into 4 looping animations, one for each way the player can face
     */
    @SuppressWarnings("unchecked")
    private static Animation<TextureRegion>[] createRowAnimations(Texture sheet, int row, float frameTime) {
        TextureRegion[] rowFrames = TextureRegion.split(sheet, FRAME_WIDTH, FRAME_HEIGHT)[row];
        Animation<TextureRegion>[] animations = new Animation[4];

        for (int direction = 0; direction < 4; direction++) {
            int start = direction * FRAMES_PER_DIRECTION;
            TextureRegion[] frames = Arrays.copyOfRange(rowFrames, start, start + FRAMES_PER_DIRECTION);
            animations[direction] = new Animation<>(frameTime, frames);
            animations[direction].setPlayMode(Animation.PlayMode.LOOP);
        }

        return animations;
    }

    public void transformIntoGoose(Texture gooseTexture) {
        if (!transformed) {
            transformed = true;
            gooseSprite = new TextureRegion(gooseTexture, 0, 0, 24, 24);
        }
    }

    public boolean isTransformed() {
        return transformed;
    }

    public void setInMud(boolean inMud) {
        this.inMud = inMud;
    }

    public void setCollision(CollisionMap collision) { this.collision = collision; }
    public void setAudio(AudioManager audio) {this.audio = audio; }

    @Override
    public void update(float deltaTime) {
        float speed = inMud ? MUD_SPEED : SPEED;
        velocity.set(InputManager.getHorizontal(), InputManager.getVertical()).nor();
        moveBy(velocity.x * speed * deltaTime, velocity.y * speed * deltaTime, collision);

        moving = !velocity.isZero();
        if (moving) {
            facing = getFacing(velocity);
        }

        // Legs move slower in mud so the feet don't look like they're sliding (FR_MUD)
        stateTime += deltaTime * (speed / SPEED);
        updateFootsteps();
    }
    private void updateFootsteps() {
        if (audio == null || transformed || !moving) {
            lastFrameIndex = -1;
            return;
        }
        int frame = walkAnimations[facing].getKeyFrameIndex(stateTime);
        if (frame != lastFrameIndex) {
            lastFrameIndex = frame;
            for (int stepFrame : STEP_FRAMES) {
                if (frame == stepFrame) {
                    audio.playRandom(Assets.SFX_FOOTSTEPS, 0.6f, 0.15f);

                    if (isHidden()) {
                        audio.playRandom(Assets.SFX_GRASS_RUSTLE, 1f, 0.2f); // NEW
                    }
                    break;
                }
            }
        }
    }

    /**
     * Picks which way to face. On diagonals the side-on walk wins, as it looks the most natural
     */
    private int getFacing(Vector2 direction) {
        if (direction.x > 0f) return FACING_RIGHT;
        if (direction.x < 0f) return FACING_LEFT;
        return direction.y > 0f ? FACING_UP : FACING_DOWN;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (transformed) {
            batch.draw(gooseSprite, position.x, position.y, 36f, 36f);
            return;
        }

        Animation<TextureRegion>[] animations = moving ? walkAnimations : idleAnimations;
        TextureRegion frame = animations[facing].getKeyFrame(stateTime);
        batch.draw(frame, position.x, position.y, DRAW_WIDTH, DRAW_HEIGHT);
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
        return transformed ? gooseSprite.getRegionWidth() : DRAW_WIDTH;
    }

    @Override
    public float getHeight() {
        return transformed ? gooseSprite.getRegionHeight() : DRAW_HEIGHT;
    }
}
