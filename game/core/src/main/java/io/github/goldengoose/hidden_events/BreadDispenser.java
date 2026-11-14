package io.github.goldengoose.hidden_events;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.world.GameWorld;
import io.github.goldengoose.goose.Bread;
import io.github.goldengoose.util.InputManager;

/**
 * FR_BREAD_DISPENSER: "Dispense bread that distracts nearby geese."
 *
 * A one-time hidden event. The dispenser sits in one spot, mostly hidden until
 * the player walks into it's trigger zone. The dispenser has 5 different states:
 *
 *      HIDDEN: Dispenser invisible --> player enters zone -->
 *      RISING: Plays rising animation --> animation ends -->
 *      READY: Player presses interact (while in the zone)-->
 *      SHAKING: Plays shaking animation --> shake ends --> bread is placed on a ring around the machine -->
 *      EMPTY: Plays IDLE animation and cannot dispense anymore bread.
 *
 * What the bread does to the geese is NOT decided here. Bread is it's own entity and gooseAi decides
 * how strongly to be pulled towards it. But each bread entity will outweigh the player.
 */

public class BreadDispenser extends HiddenEvent {

    private enum State { HIDDEN, RISING, READY, SHAKING, EMPTY }

    private static final int BREAD_COUNT = 3;
    private static final float RISE_FRAME_SECONDS = 0.1f;
    private static final float IDLE_FRAME_SECONDS = 0.4f;

    // Where the bread lands in an ellipse around the dispenser
    private static final float RING_RADIUS_X = 56f;
    private static final float RING_RADIUS_Y = 36f;
    private static final float RING_CENTRE_Y = 16f;
    private static final double RING_START_ANGLE = -Math.PI / 2;

    // Where the pieces of bread launch from - it's slot; height above dispenser base
    private static final float LAUNCH_Y = 20f;
    // Seconds between each piece of bread launching from the dispenser
    private static final float LAUNCH_STAGGER = 0.12f;

    // Shaking animation
    private static final float SHAKE_SECONDS = 0.5f;
    private static final float SHAKE_AMPLITUDE = 2.5f;
    private static final float SHAKE_FREQUENCY = 45f;

    // Prompt animation
    private static final float PROMPT_GAP = -20f;
    private static final float PROMPT_BOB = 2f;
    private static final float PROMPT_BOB_SPEED = 3f;

    private final GameWorld world;

    private final Animation<TextureRegion> rise;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> prompt;
    private final float zoneX, zoneY, zoneWidth;

    private State state = State.HIDDEN;
    private float stateTime = 0f;
    private float pulseTime = 0f;
    // used to show interact prompt
    private boolean playerInside = false;


    public BreadDispenser(GameWorld world, TextureAtlas atlas, Animation<TextureRegion> prompt,float x, float y, float width, float height) {
        super("Bread dispenser", "Bread dispenser found", x, y, width, height, 0f);

        this.world = world;
        this.zoneX = x;
        this.prompt = prompt;
        this.zoneY = y;
        this.zoneWidth = width;

        rise = new Animation<TextureRegion>(RISE_FRAME_SECONDS, atlas.findRegions("dispenser_rise"), Animation.PlayMode.NORMAL);
        idle = new Animation<TextureRegion>(IDLE_FRAME_SECONDS, atlas.findRegions("dispenser_idle"), Animation.PlayMode.LOOP);
    }

    // Player steps into zone
    @Override
    protected void onEnter(Entity entity) {
        playerInside = true; // NEW
        if (state == State.HIDDEN) {
            state = State.RISING;
            stateTime = 0f;
        }
    }

    @Override
    protected void onExit(Entity entity){
        playerInside = false;
    }

    @Override
    protected void onStay(Entity entity, float deltaTime) {
        if (state == State.READY && InputManager.interact() && trigger()) {
            state = State.SHAKING;
            stateTime = 0f;
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (state == State.HIDDEN) {
            return;
        }

        stateTime += deltaTime;
        pulseTime += deltaTime;

        if (state == State.RISING && stateTime >= rise.getAnimationDuration()) {
            // Waits fo player to interact
            state = State.READY;
            stateTime = 0f;

        } else if (state == State.SHAKING && stateTime >= SHAKE_SECONDS) {
            dispenseBread();
            state = State.EMPTY;
            stateTime = 0f;
        }
    }

    // Bread lands in an ellipse around the dispenser
    private void dispenseBread() {
        float ringX = zoneX + zoneWidth / 2f;
        float ringY = zoneY + RING_CENTRE_Y;
        for (int i = 0; i < BREAD_COUNT; i++) {
            double angle = RING_START_ANGLE + 2.0 * Math.PI * i / BREAD_COUNT;
            float breadX = ringX + (float) Math.cos(angle) * RING_RADIUS_X;
            float breadY = ringY + (float) Math.sin(angle) * RING_RADIUS_Y;

            // Each piece starts at the machine's slot and flies to it's spot, one after another.
            Vector2 from = new Vector2(ringX, zoneY + LAUNCH_Y);
            world.addEntity(new Bread(from, new Vector2(breadX, breadY), i * LAUNCH_STAGGER));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (state == State.HIDDEN) {
            return;
        }

        TextureRegion frame = (state == State.RISING)
            ? rise.getKeyFrame(stateTime, false)
            : idle.getKeyFrame(stateTime, true);

        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        float drawX = zoneX + zoneWidth / 2f - w / 2f;

        // Shake animation by offsetting sprite with sin wave
        if (state == State.SHAKING) {
            float fade = 1f - stateTime / SHAKE_SECONDS;
            drawX += (float) Math.sin(stateTime * SHAKE_FREQUENCY) * SHAKE_AMPLITUDE * fade;
        }

        batch.draw(frame, drawX, zoneY, w, h);

        boolean showPrompt = state == State.READY && playerInside;
        if (showPrompt) {
            batch.setColor(1f, 1f, 1f, 1f);
        }

        if (showPrompt && prompt != null) {
            TextureRegion icon = prompt.getKeyFrame(pulseTime, true);
            float iw = icon.getRegionWidth();
            float ih = icon.getRegionHeight();
            float bob = (float) Math.sin(pulseTime * PROMPT_BOB_SPEED) * PROMPT_BOB;
            batch.draw(icon, zoneX + zoneWidth / 2f - iw / 2f, zoneY + h + PROMPT_GAP + bob, iw, ih);
        }
    }

    public boolean isDispensed() {
        return state == State.EMPTY;
    }

    public boolean isPoppingUp() {
        return state == State.RISING;
    }

    public boolean isReady() {
        return state == State.READY;
    }
}
