package io.github.goldengoose.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.HashSet;
import java.util.Set;

/**
 * The hint at the top of the screen. Picks a message from where the player is and how far along they
 * are, and fades each one out after a few seconds.
 */
public class ObjectiveSystem {

    private String currentObjective = "";
    private float displayTimer = 0f;
    private static final float DISPLAY_TIME = 5f;
    private boolean visitedInfoDesk = false;
    private final Set<String> shownObjectives = new HashSet<>();

    private static final Rectangle INFO_DESK_ZONE = new Rectangle(380, 396, 264, 244);
    private static final Rectangle STAFF_ROOM_ZONE = new Rectangle(12, 12, 244, 302);
    private static final Rectangle IAN_WAND_DOOR_ZONE = new Rectangle(330, 160, 150, 100);

    public void update(float delta, KeyState keyState, String currentArea, Vector2 playerPos) {
        if (displayTimer > 0) displayTimer -= delta;

        boolean inRonCooke = currentArea.contains("Ron_Cooke") || currentArea.contains("ron_cooke");
        boolean inInfoDesk = inRonCooke && INFO_DESK_ZONE.contains(playerPos);

        if (inInfoDesk && keyState == KeyState.UNMAGNETISED) {
            visitedInfoDesk = true;
        }

        if (keyState == KeyState.MAGNETISED) {
            visitedInfoDesk = false;
        }

        if (!currentObjective.isEmpty() && displayTimer <= 0) {
            shownObjectives.add(currentObjective);
        }

        String newObjective = determineObjective(keyState, currentArea, playerPos);
        if (!newObjective.equals(currentObjective)) {
            currentObjective = newObjective;
            if (!newObjective.isEmpty()) {
                displayTimer = DISPLAY_TIME;
            } else {
                displayTimer = 0f;
            }
        }
    }

    private String determineObjective(KeyState keyState, String currentArea, Vector2 playerPos) {
        boolean inRonCooke = currentArea.contains("Ron_Cooke") || currentArea.contains("ron_cooke");
        boolean inIanWand = currentArea.contains("ian_wand") || currentArea.contains("Ian_Wand");
        boolean onCampus = currentArea.contains("Outside");
        boolean nearIanWandDoor = onCampus && IAN_WAND_DOOR_ZONE.contains(playerPos);
        boolean inInfoDesk = inRonCooke && INFO_DESK_ZONE.contains(playerPos);
        boolean inStaffRoom = inRonCooke && STAFF_ROOM_ZONE.contains(playerPos);

        if (keyState == KeyState.NONE && onCampus  && !shown("Find a way out")) {
            return "Find a way out";
        }
        if (keyState == KeyState.NONE && nearIanWandDoor && !shown("You need a keycard - check near the bin lorry")) {
            return "You need a keycard - check near the bin lorry";
        }
        if (keyState == KeyState.NONE && inIanWand && !shown("You need a keycard - check near the bin lorry")) {
            return "You need a keycard - check near the bin lorry";
        }
        if (keyState == KeyState.UNMAGNETISED && onCampus && !nearIanWandDoor && !shown("Try to use the keycard to get into Ian Wand")) {
            return "Try to use the keycard to get into Ian Wand";
        }
        if (keyState == KeyState.UNMAGNETISED && nearIanWandDoor && !shown("This keycard is not magnetised - go to Ron Cooke")) {
            return "This keycard is not magnetised - go to Ron Cooke";
        }
        if (keyState == KeyState.UNMAGNETISED && inIanWand && !shown("This keycard is not magnetised - go to Ron Cooke")) {
            return "This keycard is not magnetised - go to Ron Cooke";
        }
        if (keyState == KeyState.UNMAGNETISED && inStaffRoom && !shown("Use the keycard magnetiser")) {
            return "Use the keycard magnetiser";
        }
        if (keyState == KeyState.UNMAGNETISED && inInfoDesk && !shown("The keycard magnetiser is in the Staff Room")) {
            return "The keycard magnetiser is in the Staff Room";
        }
        if (keyState == KeyState.UNMAGNETISED && inRonCooke && !visitedInfoDesk && !shown("Go to the Information Desk")) {
            return "Go to the Information Desk";
        }
        if (keyState == KeyState.UNMAGNETISED && inRonCooke && visitedInfoDesk && !shown("Find the Staff Room")) {
            return "Find the Staff Room";
        }
        if (keyState == KeyState.MAGNETISED && inRonCooke && !shown("Your keycard is ready - go back to Ian Wand")) {
            return "Your keycard is ready - go back to Ian Wand";
        }
        if (keyState == KeyState.MAGNETISED && inIanWand && !shown("Find a way back home")) {
            return "Find a way back home";
        }
        return "";
    }

    private boolean shown(String objective) {
        return shownObjectives.contains(objective);
    }

    public String getCurrentObjective() { return currentObjective; }

    public boolean isVisible() { return !currentObjective.isEmpty(); }

    public float getAlpha() { return Math.min(1f, displayTimer); }
}
