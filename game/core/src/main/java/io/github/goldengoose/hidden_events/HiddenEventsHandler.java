package io.github.goldengoose.hidden_events;

import io.github.goldengoose.world.GameWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UR_HIDDEN_EVENTS: "The map contains 5 hidden events including: hiding spots, tall grass, mud, bread dispenser, boss event" (2 implement in A1 though).
 *
 * Keeps track of the hidden events on a map. It adds each event to the world, counts how many have
 * been triggered, and remembers the latest message for a few seconds so the HUD can show it (doesn't
 * currently though).
 */
public class HiddenEventsHandler implements HiddenEvent.Listener {
    private static final float MESSAGE_DURATION = 3f;

    private final GameWorld world;
    private final List<HiddenEvent> events = new ArrayList<>();

    private int totalTriggers = 0;
    private int eventsTriggered = 0;

    private String message = "";
    private float messageTimer = 0f;

    public HiddenEventsHandler(GameWorld world) {
        this.world = world;
    }

    // Add a hidden event to the game:
    public void register(HiddenEvent event) {
        events.add(event);
        event.setListener(this);
        world.addEntity(event);
    }


    public void update(float deltaTime) {
        if (messageTimer > 0f) {
            messageTimer -= deltaTime;
        }
    }

    @Override
    public void onTriggered(HiddenEvent event) {
        totalTriggers++;
        if (event.getTimesTriggered() == 1) {
            eventsTriggered++;
        }
        message = event.getFeedbackMessage();
        messageTimer = MESSAGE_DURATION;
    }

    // Number of distinct hiddenevents found so far
    public int getEventsTriggered() {
        return eventsTriggered;
    }

    // Every trigger
    public int getTotalTriggers() {
        return totalTriggers;
    }

    public int getRegisteredCount() {
        return events.size();
    }

    public List<HiddenEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    // Current feedback text
    public String getMessage() {
        return messageTimer > 0f ? message : "";
    }
}
