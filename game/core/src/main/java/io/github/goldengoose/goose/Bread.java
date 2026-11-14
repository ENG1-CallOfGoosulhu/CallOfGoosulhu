package io.github.goldengoose.goose;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.goldengoose.world.Entity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * A piece of bread dropped by the bread dispenser (FR_BREAD_DISPENSER).
 *
 * Acts as a lure for geese. Outweighs the player's importance such that chasing geese will disregard the player
 * and chase the bread to eat instead.
 *
 * To keep one piece from pulling every goose on the map, each piece only attracts
 * MAX_EATERS geese. A goose that cannot claim a slot just ignores this piece.
 *
 * Bread works like any other entity: GameWorld calls update() and interact() on it,
 * and removes it once isExpired() is true.
 */

public class Bread extends Entity {
    private static final float LIFETIME_SECONDS = 30f;
    private static final float LURE_RADIUS = 120f;
    private static final int MAX_EATERS = 2;
    private static final float SIZE = 32f;
    private static final float EAT_SECONDS = 4f;
    private static final float EAT_RANGE = 14f;
    private static final float MIN_SCALE = 0.4f;

    // Bread's flight from dispenser slot to final position
    private static final float FLIGHT_SECONDS = 0.6f;
    private static final float ARC_HEIGHT = 40f;
    private static final float SHADOW_ALPHA = 0.3f;

    private static TextureRegion sprite;
    private static Animation<TextureRegion> spin;

    // Identity set
    private final Set<Goose> eaters = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Vector2 otherCentre = new Vector2();
    private final Vector2 centre = new Vector2();
    private float age = 0f;
    private float foodLeft = EAT_SECONDS;
    private float frameDelta = 0f;

    // Bread flight. A piece of bread that is flying does not age and is not lure-able
    private boolean flying = false;
    private float flightClock = 0f;
    private float launchDelay = 0f;
    private float arcHeight = 0f;
    private final Vector2 launchFrom = new Vector2();
    private final Vector2 landingAt = new Vector2();

    // Where the bread lies on the map
    public Bread(Vector2 centre) {
        position.set(centre.x - SIZE / 2f, centre.y - SIZE / 2f);
    }

    public Bread(Vector2 from, Vector2 to, float delay) {
        launchFrom.set(from);
        landingAt.set(to);
        launchDelay = delay;
        flying = true;
        position.set(from.x - SIZE / 2f, from.y - SIZE / 2f);
    }
    public static void setSprite(TextureRegion region) {
        sprite = region;
    }

    public static void setSpinAnimation(Animation<TextureRegion> animation) {
        spin = animation;
    }

    @Override
    public void update(float deltaTime) {
        frameDelta = deltaTime;
        // Bread doesn't age until it has landed
        if (flying) {
            updateFlight(deltaTime);
            return;
        }
        age += deltaTime;
    }

    private void updateFlight(float deltaTime) {
        flightClock += deltaTime;
        float t = (flightClock - launchDelay) / FLIGHT_SECONDS;
        if (t < 0f) {
            return;
        }
        if (t >= 1f) {
            flying = false;
            arcHeight = 0f;
            position.set(landingAt.x - SIZE / 2f, landingAt.y - SIZE / 2f);
            return;
        }

        position.set(launchFrom.x + (landingAt.x - launchFrom.x) * t - SIZE / 2f, launchFrom.y + (landingAt.y - launchFrom.y) * t - SIZE / 2f);
        arcHeight = 4f * ARC_HEIGHT * t * (1f - t);
    }

    @Override
    public void interact(Entity other) {
        if (flying|| isExpired() || !(other instanceof Goose goose)) {
            return;
        }
        getCentre(centre);
        other.getCentre(otherCentre);
        float dist2 = centre.dst2(otherCentre);
        if (centre.dst2(otherCentre) <= LURE_RADIUS * LURE_RADIUS) {
            goose.distractBy(this);
        }
        // Two geese eat twice as much food
        if (eaters.contains(goose) && dist2 <= EAT_RANGE * EAT_RANGE) {
            foodLeft -= frameDelta;
        }
    }


    public boolean tryClaim(Goose goose) {
        if (isExpired() || flying) {
            return false;
        }
        if (eaters.contains(goose)) {
            return true;
        }
        if (eaters.size() >= MAX_EATERS) {
            return false;
        }
        eaters.add(goose);
        return true;
    }

    public int getEaterCount() {
        return eaters.size();
    }

    @Override
    // Bread vanishes if it's either eaten by geese or reaches it expires
    public boolean isExpired() {
        return age >= LIFETIME_SECONDS || foodLeft <= 0f;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (flying && flightClock < launchDelay) {
            return;
        }

        TextureRegion frame = sprite;
        // Flying == spinning, landed = resting on the first frame
        if (spin != null) {
            frame = flying ? spin.getKeyFrame(flightClock - launchDelay, true) : spin.getKeyFrame(0f, false);
        }

        if (frame == null) {
            return;
        }

        if (flying) {
            batch.setColor(0f, 0f, 0f, SHADOW_ALPHA);
            batch.draw(frame, position.x, position.y, SIZE, SIZE / 2f);
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(frame, position.x, position.y + arcHeight, SIZE, SIZE);
            return;
        }
        float size = SIZE * (MIN_SCALE + (1f - MIN_SCALE) * Math.max(foodLeft, 0f) / EAT_SECONDS);
        batch.draw(frame, position.x + (SIZE - size) / 2f, position.y + (SIZE - size) / 2f, size, size);
    }

    public void renderDebug(ShapeRenderer shapes) {
        if (isExpired()) {
            return;
        }
        getCentre(centre);
        shapes.setColor(Color.ORANGE);
        shapes.circle(centre.x, centre.y, 4f);

        if (!flying) {
            shapes.setColor(Color.YELLOW);
            shapes.circle(centre.x, centre.y, LURE_RADIUS);
        }
    }

    @Override
    public float getWidth() {
        return SIZE;
    }

    @Override
    public float getHeight() {
        return SIZE;
    }
}
