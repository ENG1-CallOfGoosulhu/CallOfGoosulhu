package io.github.goldengoose.hidden_events;

import io.github.goldengoose.world.Entity;

/**
 * Placeholder event for checking the handler works.
 * Displays how to use HiddenEvent/HiddenEventHandler
 */

public class TestHiddenEvent extends HiddenEvent {

    public TestHiddenEvent(float x, float y, float width, float height) {
        super("Test event", "Hidden event found!", x, y, width, height, 0f);
    }

    @Override
    protected void onEnter(Entity entity) {
        trigger();
    }
}
