package io.github.goldengoose.goose;


import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.world.CollisionMap;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.util.Utilities;
import io.github.goldengoose.audio.AudioManager;

public class Goose extends Entity {
    //------------
    // CONSTANTS
    //------------

    // For the "?" that appears above a confused goose AND the "!" that appears above alerted geese
    private static TextureRegion alertIcon;
    private static TextureRegion confusedIcon;
    private static float ICON_GAP = 2f;
    private static final float ICON_BOB = 1.5f;
    private static final float ICON_POP_SECONDS = 0.15f;
    private static final float ICON_FADE_SECONDS = 0.3f;
    private static final float ICON_SCALE = 1.5f;

    public static void setAlertIcon(TextureRegion region){
        alertIcon = region;
    }

    public static void setConfusedIcon(TextureRegion region){
        confusedIcon = region;
    }

    // Size of each frame
    private static final int SOURCE_SIZE = 24;
    // Size to draw them, slightly larger to reduce mixelation
    private static final float DRAW_SIZE = 36f;
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
    private final CollisionMap collision;

    private AudioManager audio;
    private boolean honk;
    private float trappedTimer = 0f;

    private final Animation<TextureRegion>[] walkAnimations;
    private final Animation<TextureRegion>[] runAnimations;

    private float stateTime = 0f;

    public Goose(Player player, Vector2 position, CollisionMap collision, Assets assets) {
        walkAnimations = Utilities.createAnimations(assets.getTexture(Assets.GOOSE_WALK), WALK_FRAME_TIME, SOURCE_SIZE);
        runAnimations = Utilities.createAnimations(assets.getTexture(Assets.GOOSE_RUN), RUN_FRAME_TIME, SOURCE_SIZE);
        this.collision = collision;

        this.player = player;
        this.position.set(position);
    }

    @Override
    public void update(float deltaTime) {
        if (trappedTimer > 0f) {
            trappedTimer -= deltaTime;
            // A goose that has been trapped between other geese isn't updating, so don t let the pushes pile up
            gooseAI.clearSeparation();
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

        Animation<TextureRegion>[] animations = gooseAI.isRunning() ? runAnimations : walkAnimations;
        TextureRegion frame = animations[row].getKeyFrame(stateTime);

        if (flip) {
            batch.draw(frame, position.x + DRAW_SIZE, position.y, -DRAW_SIZE, DRAW_SIZE);
        } else {
            batch.draw(frame, position.x, position.y, DRAW_SIZE, DRAW_SIZE);
        }

        if (gooseAI.isAlerted()) {
            renderIcon(batch, alertIcon, gooseAI.getAlertElapsed(), gooseAI.getAlertRemaining());

        } else if (gooseAI.isConfused()){
            renderIcon(batch, confusedIcon, gooseAI.getConfusedElapsed(), gooseAI.getConfusedRemaining());
        }
    }

    // The "?" and "!" fade in and out above the goose's head
    private void renderIcon(SpriteBatch batch, TextureRegion icon, float elapsed, float remaining){
        if (icon == null){
            return;
        }

        float pop = Math.min(1f, elapsed / ICON_POP_SECONDS);
        float alpha = Math.min(1f, remaining / ICON_FADE_SECONDS);
        float w = icon.getRegionWidth() * pop * ICON_SCALE;
        float h = icon.getRegionHeight() * pop * ICON_SCALE;
        float bob = (float) Math.sin(elapsed * 6f) * ICON_BOB;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(icon, position.x + DRAW_SIZE / 2f - w / 2f, position.y + DRAW_SIZE + ICON_GAP + bob, w, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    //GameWorld calls this for every pair of entities each frame
    public void interact(Entity other) {
        if (other instanceof Goose otherGoose) {
            gooseAI.addSeparationFrom(otherGoose);
        }
    }
    public void setAudio(AudioManager audio){
        this.audio = audio;
    }

    public void onAlerted(){
        if (audio == null) return;
        float cx = position.x + getWidth() / 2f;
        float cy = position.y + getHeight() / 2f;
        audio.playRandomAt(Assets.SFX_GOOSE_HONK, cx, cy, 1f, 0.12f);
    }
    public void distractBy(Bread bread) {
        gooseAI.distractBy(bread);
    }

    public void scareFrom(Vector2 source) {
        gooseAI.scareFrom(source);
    }

    // True if standing on honk ring on piece of bread
    public boolean isSettled() {
        return gooseAI.isSettled();
    }

    // True whilst this goose is standing on the honk ring
    public boolean isSwarming(){
        return gooseAI.isSwarming();
    }

    public boolean isChasing(){
        return gooseAI.isChasing();
    }

    public void renderDebug(ShapeRenderer shapes) {
        gooseAI.renderDebug(shapes);
    }

    public Player getPlayer() {
        return player;
    }

    public CollisionMap getCollision() {  return collision; }

    @Override
    public float getWidth() {
        return DRAW_SIZE;
    }

    @Override
    public float getHeight() {
        return DRAW_SIZE;
    }

    public void trap(float duration) {
        trappedTimer = Math.max(trappedTimer, duration);
    }

    public boolean isTrapped() {
        return trappedTimer > 0f;
    }
}
