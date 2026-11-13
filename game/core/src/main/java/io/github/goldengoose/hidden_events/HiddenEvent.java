package io.github.goldengoose.hidden_events;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.world.Entity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * This is a base class for every hidden event
 *
 * An event is an entity that owns a rectangular zone, calling GameWorld's interact(other) to check
 * if the player is standing inside the zone.
 *
 * HiddenEvent has 5 hooks - optional methods. So a concrete event should override the hooks it cares about:
 *  onEnter - something stepped into the zone
 *  onExit - something left the zone
 *  onStay - something is still in the zone (checked every frame)
 *  onUpdate - logic of its own e.g. timers, animation
 * All call trigger() the moment the event has counted as happened.
 *
 * The 5th hook is affects() which decided who is detected. The player by default. But should an event want to
 * react to geese for example (bread dispenser) then override it.
 *
 */

public abstract class HiddenEvent extends Entity {
    public interface Listener {
        void onTriggered(HiddenEvent event);
    }

    private final String name;
    private final String feedbackMessage; // shown on screen when triggered (QR_SOUND_ACCESS: never sound only)
    private final float width;
    private final float height;
    private final float cooldownSeconds;  // minimum time between triggers; 0 = no limit

    // Apparently HashSet equals() is a poor comparitor of whether an entity occupies the exact same space in memory
    // So Using IdentityHashMaps instead.
    // The two sets are needed because the event can't know whether an entity has left without an entire frame passing.
    // Interact() only reports who is present, not who isn't. So a comparison of the two sets needs to be done every frame to figure out whether someone has left.
    private final Set<Entity> inside = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Entity> seenThisFrame = Collections.newSetFromMap(new IdentityHashMap<>());

    private final Vector2 otherCentre = new Vector2();

    private Listener listener;
    private float cooldownRemaining = 0f;
    private int timesTriggered = 0;
    private boolean revealed = false;


    protected HiddenEvent(String name, String feedbackMessage, float x, float y, float width, float height, float cooldownSeconds) {
        this.name = name;
        this.feedbackMessage = feedbackMessage;
        this.width = width;
        this.height = height;
        this.cooldownSeconds = cooldownSeconds;
        this.position.set(x, y);
    }


    // Hooks for subclasses
    // By default "entity" is the player - use affected() to change this.
    protected boolean affects(Entity other) {
        return other instanceof Player;
    }

    protected void onEnter(Entity entity) {
    }
    protected void onStay(Entity entity, float deltaTime) {
    }
    protected void onExit(Entity entity) {
    }
    protected void onUpdate(float deltaTime) {
    }

    protected final boolean trigger() {
        if (cooldownRemaining > 0f) {
            return false;
        }
        timesTriggered++;
        revealed = true;
        cooldownRemaining = cooldownSeconds;
        if (listener != null) {
            listener.onTriggered(this);
        }
        return true;
    }


    @Override
    public final void update(float deltaTime) {
        Iterator<Entity> it = inside.iterator();
        while (it.hasNext()) {
            Entity entity = it.next();
            if (!seenThisFrame.contains(entity)) {
                it.remove();
                onExit(entity);
            }
        }
        seenThisFrame.clear();

        if (cooldownRemaining > 0f) {
            cooldownRemaining -= deltaTime;
        }

        for (Entity entity : inside) {
            onStay(entity, deltaTime);
        }
        onUpdate(deltaTime);
    }


    @Override
    public final void interact(Entity other) {
        if (other == this || !affects(other) || !contains(other)) {
            return;
        }
        seenThisFrame.add(other);
        if (inside.add(other)) { // add() is true only the first frame it is inside
            onEnter(other);
        }
    }

    // True if the entity's CENTRE is within the rectangle
    public boolean contains(Entity other) {
        other.getCentre(otherCentre);
        return otherCentre.x >= position.x && otherCentre.x <= position.x + width
            && otherCentre.y >= position.y && otherCentre.y <= position.y + height;
    }


    @Override
    public void render(SpriteBatch batch) { }

    // outline of the trigger zone
    public void renderDebug(ShapeRenderer shapes) {
        if (timesTriggered > 0) {
            shapes.setColor(0.2f, 1f, 0.2f, 1f);
        } else {
            shapes.setColor(1f, 0f, 1f, 1f);
        }
        shapes.rect(position.x, position.y, width, height);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public int getTimesTriggered() {
        return timesTriggered;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean canTrigger() {
        return cooldownRemaining <= 0f;
    }

    public boolean isInside(Entity entity) {
        return inside.contains(entity);
    }
}
