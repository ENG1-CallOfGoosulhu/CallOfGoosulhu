package io.github.goldengoose.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class ObjectiveSystem {

    private String currentObjective = "";
    private float displayTimer = 0f;
    private static final float DISPLAY_TIME = 5f;

    private static final Rectangle INFO_DESK_ZONE = new Rectangle(380, 396, 264, 244);
    private static final Rectangle STAFF_ROOM_ZONE = new Rectangle(12, 12, 244, 302);
    private static final Rectangle IAN_WAND_DOOR_ZONE = new Rectangle(330, 160, 150, 100);
    public void update(float delta, KeyState keyState, String currentArea, Vector2 playerPos) {
        if (displayTimer > 0) displayTimer -= delta;

        String newObjective = determineObjective(keyState, currentArea, playerPos);
        if (!newObjective.equals(currentObjective)) {
            currentObjective = newObjective;
            displayTimer = DISPLAY_TIME;
        }
    }

    private String determineObjective(KeyState keyState, String currentArea, Vector2 playerPos) {
        boolean inRonCooke = currentArea.contains("Ron_Cooke") || currentArea.contains("ron_cooke");
        boolean inIanWand = currentArea.contains("ian_wand") || currentArea.contains("Ian_Wand");
        boolean onCampus = currentArea.contains("Outside");
        boolean nearIanWandDoor = onCampus && IAN_WAND_DOOR_ZONE.contains(playerPos);
        boolean inInfoDesk = inRonCooke && INFO_DESK_ZONE.contains(playerPos);
        boolean inStaffRoom = inRonCooke && STAFF_ROOM_ZONE.contains(playerPos);

        if (keyState == KeyState.NONE && onCampus && !nearIanWandDoor) {
            return "Find a way out";
        }
        if (keyState == KeyState.NONE && nearIanWandDoor) {
            return "You need a keycard - check near the bin lorry";
        }
        if (keyState == KeyState.NONE && inIanWand) {
            return "You need a keycard - check near the bin lorry";
        }
        if (keyState == KeyState.UNMAGNETISED && onCampus && !nearIanWandDoor) {
            return "Try to use the keycard to get into Ian Wand";
        }
        if (keyState == KeyState.UNMAGNETISED && nearIanWandDoor) {
            return "This keycard is not magnetised - go to Ron Cooke";
        }
        if (keyState == KeyState.UNMAGNETISED && inIanWand) {
            return "This keycard is not magnetised - go to Ron Cooke";
        }
        if (keyState == KeyState.UNMAGNETISED && inStaffRoom) {
            return "Use the keycard magnetiser";
        }
        if (keyState == KeyState.UNMAGNETISED && inInfoDesk) {
            return "The keycard magnetiser is in the Staff Room";
        }
        if (keyState == KeyState.UNMAGNETISED && inRonCooke) {
            return "Go to the Information Desk";
        }
        if (keyState == KeyState.MAGNETISED && inRonCooke) {
            return "Your keycard is ready - go back to Ian Wand";
        }
        if (keyState == KeyState.MAGNETISED && onCampus) {
            return "Enter Ian Wand";
        }
        if (keyState == KeyState.MAGNETISED && inIanWand) {
            return "Find a way back home";
        }
        return "";
    }

    public String getCurrentObjective() { return currentObjective; }

    public boolean isVisible() { return !currentObjective.isEmpty(); }

    public float getAlpha() { return Math.min(1f, displayTimer); }
}
