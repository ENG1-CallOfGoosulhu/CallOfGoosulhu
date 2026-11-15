package io.github.goldengoose.hidden_events;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import io.github.goldengoose.util.InputManager;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.world.GameWorld;
import io.github.goldengoose.world.KeyState;

/**
 * Keycard magnetiser in Ron Cooke's staff room. If the player stands at it with the unmagnetised
 * keycard and presses E, the keycard becomes magnetised and can open Ian Wand. Shows the E prompt
 * while the player is in range.
 */
public class KeycardMagnetiser extends HiddenEvent {
    // Prompt animation
    private static final float PROMPT_GAP = -20f;
    private static final float PROMPT_BOB = 2f;
    private static final float PROMPT_BOB_SPEED = 3f;

    private float pulseTime = 0f;

    private final GameWorld world;
    private float stateTime = 0f;
    private final Animation<TextureRegion> prompt;
    private final float zoneX, zoneY, zoneWidth;

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
    @Override protected void onUpdate(float deltaTime) { pulseTime += deltaTime;}


    @Override
    protected void onStay(Entity entity, float deltaTime) {
        if (world.getKeyState() == KeyState.UNMAGNETISED && InputManager.interact() && trigger()) {
            stateTime = 0f;
            world.setKeyState(KeyState.MAGNETISED);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (world.getKeyState() != KeyState.UNMAGNETISED) {
            return;
        }

        boolean showPrompt = world.getKeyState() == KeyState.UNMAGNETISED && playerInside;
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
