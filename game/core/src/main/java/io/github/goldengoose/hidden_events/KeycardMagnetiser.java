package io.github.goldengoose.hidden_events;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import io.github.goldengoose.util.InputManager;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.world.GameWorld;

public class KeycardMagnetiser extends HiddenEvent {
    private enum State { AVAILABLE, UNAVAILABLE }

    // Prompt animation
    private static final float PROMPT_GAP = -20f;
    private static final float PROMPT_BOB = 2f;
    private static final float PROMPT_BOB_SPEED = 3f;

    private float pulseTime = 0f;

    private final GameWorld world;
    private float stateTime = 0f;
    private final Animation<TextureRegion> prompt;
    private final float zoneX, zoneY, zoneWidth;


    private KeycardMagnetiser.State state = State.AVAILABLE;
    private boolean playerInside = false;


    public KeycardMagnetiser(GameWorld world, Animation<TextureRegion> prompt, Rectangle rect) {
        super("Keycard", "Keycard found", rect.x, rect.y, rect.width, rect.height, 0f);

        this.world = world;
        this.prompt = prompt;
        this.zoneX = rect.x - rect.width/4;
        this.zoneY = rect.y + rect.height;
        this.zoneWidth = rect.width;
    }

    @Override protected void onEnter(Entity entity) { playerInside = true; }
    @Override protected void onExit(Entity entity) { playerInside = false; }
    @Override protected void onUpdate(float deltaTime) { pulseTime += deltaTime; }


    @Override
    protected void onStay(Entity entity, float deltaTime) {
        if (state == State.AVAILABLE && InputManager.interact() && trigger()) {
            state = State.UNAVAILABLE;
            stateTime = 0f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (state == State.UNAVAILABLE) {
            return;
        }

        boolean showPrompt = state == State.AVAILABLE && playerInside;
        if (showPrompt) {
            batch.setColor(1f, 1f, 1f, 1f);
        }

        if (showPrompt && prompt != null) {
            TextureRegion icon = prompt.getKeyFrame(pulseTime, true);
            float iw = icon.getRegionWidth();
            float ih = icon.getRegionHeight();
            float bob = (float) Math.sin(pulseTime * PROMPT_BOB_SPEED) * PROMPT_BOB;
            batch.draw(icon, zoneX + zoneWidth / 2f - iw / 2f, zoneY + PROMPT_GAP + bob, iw, ih);
        }
    }
}
