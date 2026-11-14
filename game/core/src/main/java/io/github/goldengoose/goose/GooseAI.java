package io.github.goldengoose.goose;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Goose AI plan:
 * IDLE: Wander around the campus, has cone-like visual detection, circular audial detection
 * ALERT: possibly not needed
 * CHASING: Chasing the player
 * SWARMING: Attempt to surround the player
 */
public class GooseAI {
    enum GooseState {
        IDLE,
        CHASING,
        SWARMING,
        // Walking to / At a piece of bread
        DISTRACTED,
        FLEEING
    }

    //------------------------------------------------------------------
    // CONSTANTS
    //------------------------------------------------------------------
    private static final float IDLE_SPEED = 20f;
    private static final float CHASING_SPEED = 40f;
    private static final float FLEE_SPEED = 50f;

    // Min and max x/y dist goose will wander from current pos when idle
    private static final float MIN_WANDER_RANGE = 40f;
    private static final float MAX_WANDER_RANGE = 80f;

    private static final float FLEE_LINGER = 0.5f;

    // Min and max time goose will wait after idle walk
    private static final float MIN_IDLE_WAIT = 5f;
    private static final float MAX_IDLE_WAIT = 10f;

    // Detection
    private static final float VISION_RANGE = 150f;
    private static final float VISION_HALF_ANGLE_DEGREES = 45f;
    private static final float HEARING_RANGE = 100f;

    // Distance from where the Goose will stop following player and start honking
    private static final float HONK_RANGE = 50f;
    // Range from which it will still follow the player, ie simply running far enough won't make the goose forget
    private static final float FOLLOW_RANGE = 200f;

    // Geese swarming separation behaviour:
    // Running geese closer than this push each other apart
    private static final float SEPARATION_RADIUS_RUNNING = 24f;
    // Swarming geese stand closer together than running ones
    private static final float SEPARATION_RADIUS_STANDING = 16f;
    private static final float SEPARATION_WEIGHT = 1.6f;
    // pushes weaker than this are ignored - to prevent twitching
    private static final float SEPARATION_DEADZONE = 0.05f;
    private static final float HONK_RESUME_MARGIN = 10f;
    private static final float CHASE_TARGET_RADIUS = HONK_RANGE * 0.9f;
    // Geese that are pushed out of the swarming ring by this much is pulled back
    private static final float RING_TOLERANCE = 1f;
    // How long a goose will stand still, puzzled with a "?" above their head
    private static final float CONFUSED_SECONDS = 1.5f;
    // How long a goose has an "!" above their head
    private static final float ALERT_SECONDS = 1.0f;
    // How strongly geese push each other around the player
    private static final float SWARM_SLIDE_WEIGHT = 1f;
    // A goose counts as "eating bread" if they are within this distance
    private static final float EAT_DISTANCE = 12f;
    // A goose will only go back to walking if it's pushed this far off the bread (due to separation behaviour)
    private static final float EAT_RELEASE_MARGIN = 8f;
    //------------------------------------------------------------------
    // END CONSTANTS
    //------------------------------------------------------------------


    private GooseState state = GooseState.IDLE;
    private float alertTimer = 0f;
    private float confusedTimer = 0f;
    private final Goose goose;
    private final Vector2 velocity = new Vector2();
    private final Vector2 direction = new Vector2();

    private final Vector2 idleTarget = new Vector2();
    private boolean hasIdleTarget = false;

    private boolean idleWaiting = false;
    private float waitTimer;


    private final Vector2 facingDirection = new Vector2(1f, 0f);
    private final Vector2 toPlayer = new Vector2();
    private final Vector2 separation = new Vector2();
    // Vector to other goose's center, used for separation
    private final Vector2 otherCentre = new Vector2();
    private final Vector2 awayFromOther = new Vector2();
    // The point on the ring this goose is heading for
    private final Vector2 ringTarget = new Vector2();
    private final Vector2 radial = new Vector2();
    private Bread bread;
    private boolean eating = false;
    private final Vector2 breadCentre = new Vector2();
    private final Vector2 toBread = new Vector2();
    private static final float VISION_DOT_THRESHOLD = (float) Math.cos(Math.toRadians(VISION_HALF_ANGLE_DEGREES));

    private final Vector2 fleeFrom = new Vector2();
    private float fleeTimer = 0f;

    private final Vector2 gooseCentre = new Vector2();
    private final Vector2 playerCentre = new Vector2();

    public GooseAI(Goose goose) {
        this.goose = goose;
    }


    public void update(float deltaTime) {
        goose.getCentre(gooseCentre);
        goose.getPlayer().getCentre(playerCentre);
        float playerDist2 = gooseCentre.dst2(playerCentre);

        // When the bread is gone
        if (bread != null && bread.isExpired()) {
            bread = null;
            eating = false;
            if (state == GooseState.DISTRACTED) {
                // Goose goes back to IDLE behaviour once the bread has disappeared
                state = GooseState.IDLE;
            } // NEW
        } // NEW

        if (fleeTimer > 0f) {
            fleeTimer -= deltaTime;
            state = GooseState.FLEEING;
        } else if (state == GooseState.FLEEING) {
            state = GooseState.IDLE;
        }

        // Geese can no longer hear/see the player when they are hiding in the tall grass (isHidden())
        boolean playerHidden = goose.getPlayer().isHidden();
        boolean detected = !playerHidden && (canSeePlayer() || canHearPlayer(true));

        // TODO: Once we determine how player makes noise, replace true with variable:

        if (state != GooseState.IDLE) {
            if (playerDist2 > FOLLOW_RANGE * FOLLOW_RANGE) {
                state = detected ? state : GooseState.IDLE;
            }
        } else if (detected) {
            state = GooseState.CHASING;
            alertTimer = ALERT_SECONDS;
            goose.onAlerted();
        }

        // Swarming goose goes back to chasing once player has left this range
        float resumeRange = HONK_RANGE + HONK_RESUME_MARGIN;
        if (state == GooseState.CHASING && playerDist2 <= HONK_RANGE * HONK_RANGE) {
            state = GooseState.SWARMING;
        } else if (state == GooseState.SWARMING && playerDist2 > resumeRange * resumeRange) {
            state = GooseState.CHASING;
        }

        if (confusedTimer > 0f){
            confusedTimer = Math.max(0f, confusedTimer - deltaTime);
        }

        if (alertTimer > 0f) {
            alertTimer = Math.max(0f, alertTimer - deltaTime);
        }

        // Geese give up chasing the player when they're hiding
        // Displaying their confused "?"
        if (playerHidden) {
            if ((state == GooseState.CHASING || state == GooseState.SWARMING) && bread == null){
                confusedTimer = CONFUSED_SECONDS;
            }
            // Returning to their IDLE state
            state = GooseState.IDLE;
        }

        // Bread is more enticing than the player
        if (bread != null) {
            state = GooseState.DISTRACTED;
        }

        if (state != GooseState.CHASING && state != GooseState.SWARMING){
            alertTimer = 0f;
        }

        if (state != GooseState.IDLE){
            confusedTimer = 0f;
        }

        velocity.setZero();

        if (state == GooseState.FLEEING) {
            hasIdleTarget = false;
            idleWaiting = false;
            flee();
        }
        else if (state == GooseState.CHASING) {
            hasIdleTarget = false;
            idleWaiting = false;

            // aim for the point on the ring that is on this goose's own side of the player
            // From player to this goose
            radial.set(gooseCentre).sub(playerCentre);
            if (radial.isZero()) {
                radial.set(1f, 0f);
            }

            radial.nor();
            ringTarget.set(playerCentre).mulAdd(radial, CHASE_TARGET_RADIUS);
            moveTo(ringTarget);
            // Chasing geese get the full push separation
            applySeparation();

        } else if (state == GooseState.DISTRACTED) {
            hasIdleTarget = false;
            idleWaiting = false;
            feed();

        } else if (state == GooseState.SWARMING) {
            hasIdleTarget = false;
            idleWaiting = false;
            // Stand on ring, surround player on ring.
            swarm();

        // Puzzled goose will stand still and look around until ? gone.
        } else if (confusedTimer > 0f) {
            hasIdleTarget = false;
            idleWaiting = false;
            separation.setZero();

            // TODO: Honk at the player.

        } else {
            wander(deltaTime);
            applySeparation();
        }

        float speed = state == GooseState.FLEEING ? FLEE_SPEED
            : state != GooseState.IDLE   ? CHASING_SPEED
            : IDLE_SPEED;

        //goose.getPosition().mulAdd(velocity, speed * deltaTime);
        goose.moveBy(velocity.x * speed * deltaTime, velocity.y * speed * deltaTime, goose.getCollision());
    }

    private void wander(float deltaTime) {
        if (!hasIdleTarget) {
            chooseNewTarget();
            return;
        }

        if (idleWaiting) {
            waitTimer -= deltaTime;
            velocity.setZero();

            if (waitTimer <= 0f) {
                hasIdleTarget = false;
                idleWaiting = false;
            }

            return;
        }

        if (moveTo(idleTarget)) {
            velocity.setZero();
            idleWaiting = true;

            waitTimer = MathUtils.random(MIN_IDLE_WAIT, MAX_IDLE_WAIT);
        }
    }


    private void chooseNewTarget() {
        float angle = MathUtils.random(MathUtils.PI2);
        float distance = MathUtils.random(MIN_WANDER_RANGE, MAX_WANDER_RANGE);

        idleTarget.set(gooseCentre).add(MathUtils.cos(angle) * distance, MathUtils.sin(angle) * distance);

        hasIdleTarget = true;
        idleWaiting = false;
    }

    private boolean canHearPlayer(boolean playerMakingNoise) {
        if (!playerMakingNoise) {
            return false;
        }

        return gooseCentre.dst2(playerCentre) <= HEARING_RANGE * HEARING_RANGE;
    }

    private boolean canSeePlayer() {
        toPlayer.set(playerCentre).sub(gooseCentre);

        if (toPlayer.len2() > VISION_RANGE * VISION_RANGE) {
            return false;
        }

        toPlayer.nor();

        return facingDirection.dot(toPlayer) >= VISION_DOT_THRESHOLD;
    }

    private boolean moveTo(Vector2 target) {
        direction.set(target).sub(gooseCentre);

        if (direction.len2() < 3f * 3f) {
            velocity.setZero();
            return true;
        }
        velocity.set(direction).nor();

        facingDirection.set(velocity);
        return false;
    }

    // Keep geese from stacking on top of each other and turning into a "blob"
    // Works by comparing distance between each goose every other frame and pushing them apart if so
    public void addSeparationFrom(Goose other) {
        boolean eitherSwarming = isSettled() || other.isSettled();
        // Crowds stand closer together than running geese
        float radius = eitherSwarming ? SEPARATION_RADIUS_STANDING : SEPARATION_RADIUS_RUNNING;
        other.getCentre(otherCentre);
        goose.getCentre(awayFromOther);
        awayFromOther.sub(otherCentre);
        float dist = awayFromOther.len();
        if (dist >= radius) {
            return;
        }
        // Two geese are exactly ontop of each other
        if (dist < 0.001f) {
            // So choose random direction for them to separate
            awayFromOther.set(1f, 0f).setAngleDeg(MathUtils.random(360f));
        }
        // forces geese to push harder apart when they're closer together - fading out as they get further away
        float strength = 1f - dist / radius;
        separation.add(awayFromOther.nor().scl(strength));
    }

    // Behaviour while standing on the honk ring. Geese attempt to spread out along the ring around the player
    private void swarm() {
        toPlayer.set(playerCentre).sub(gooseCentre);
        if (!toPlayer.isZero()) {
            facingDirection.set(toPlayer).nor();
        }

        radial.set(gooseCentre).sub(playerCentre);
        float dist = radial.len();
        if (dist < 0.001f) {
            radial.set(1f, 0f);
        } else {
            radial.scl(1f / dist);
        }

        // Keeps the geese separation to only trying to offset each other around the ring by removing any push towards/away from player.
        separation.mulAdd(radial, -separation.dot(radial));
        if (separation.len2() < SEPARATION_DEADZONE * SEPARATION_DEADZONE) {
            separation.setZero();
        }
        velocity.set(separation).scl(SWARM_SLIDE_WEIGHT);
        if (dist > HONK_RANGE + RING_TOLERANCE) {
            velocity.mulAdd(radial, -1f);
        } else if (dist < HONK_RANGE - RING_TOLERANCE) {
            velocity.mulAdd(radial, 1f);
        }
        velocity.limit(1f);
        separation.setZero();
    }


    public void distractBy(Bread newBread) {
        if (bread != null && !bread.isExpired()) {
            return;
        }
        // Only accepted if bread has a free eating slot
        if (newBread.tryClaim(goose)) {
            bread = newBread;
            eating = false;
        }
    }

    public void scareFrom(Vector2 source) {
        fleeFrom.set(source);
        fleeTimer = FLEE_LINGER;
    }

    private void flee() {
        velocity.set(gooseCentre).sub(fleeFrom);
        if (velocity.isZero()) {
            velocity.set(1f, 0f).setAngleDeg(MathUtils.random(360f));
        }
        velocity.nor();
        facingDirection.set(velocity);
        applySeparation();
    }

    // Geese behaviour whilst distracted: Walking to bread, then standing by it (eating)
    private void feed() {
        bread.getCentre(breadCentre);
        toBread.set(breadCentre).sub(gooseCentre);
        float dist = toBread.len();
        if (dist > 0.001f) {
            facingDirection.set(toBread).nor();
        }
        if (dist <= EAT_DISTANCE) {
            eating = true;
        } else if (dist > EAT_DISTANCE + EAT_RELEASE_MARGIN) {
            eating = false;
        }

        if (!eating) {
            velocity.set(toBread).nor();
            applySeparation();
            return;
        }

        if (separation.len2() < SEPARATION_DEADZONE * SEPARATION_DEADZONE) {
            separation.setZero();
        }
        velocity.set(separation);
        if (dist > EAT_DISTANCE) {
            velocity.mulAdd(toBread.nor(), 1f);
        }
        velocity.limit(1f);
        separation.setZero();
    }

    public boolean isSettled() {
        return state == GooseState.SWARMING || (state == GooseState.DISTRACTED && eating);
    }

    // Run animation play when gong to bread?
    public boolean isRunning() {
        return state == GooseState.CHASING || state == GooseState.FLEEING
            || (state == GooseState.DISTRACTED && !eating);
    }

    public boolean isConfused() {
        return confusedTimer > 0f;
    }

    public float getConfusedElapsed(){
        return CONFUSED_SECONDS - confusedTimer;
    }

    public float getConfusedRemaining(){
        return confusedTimer;
    }

    public boolean isAlerted(){
        return alertTimer > 0f;
    }

    public float getAlertElapsed(){
        return ALERT_SECONDS - alertTimer;
    }

    public float getAlertRemaining(){
        return alertTimer;
    }

    // Allows other geese to see if this one is settled in on the ring.
    public boolean isSwarming() {
        return state == GooseState.SWARMING;
    }

    public void clearSeparation() {
        separation.setZero();
    }

    private void applySeparation() {
        if (separation.len2() < SEPARATION_DEADZONE * SEPARATION_DEADZONE) {
            // Tiny push - ignore it
            separation.setZero();
            return;
        }
        // Movement direction + weighted push
        velocity.add(separation.scl(SEPARATION_WEIGHT));
        velocity.limit(1f);
        separation.setZero();
    }

    public boolean isMoving() {
        return !velocity.isZero();
    }

    public boolean isChasing() {
        return state == GooseState.CHASING;
    }

    public Vector2 getFacingDirection() {
        return facingDirection;
    }

    public void renderDebug(ShapeRenderer shapes) {
        goose.getCentre(gooseCentre);

        float x = gooseCentre.x;
        float y = gooseCentre.y;

        // Hearing radius: circular range.
        shapes.setColor(0f, 0.7f, 1f, 1f);
        shapes.circle(x, y, HEARING_RANGE, 48);

        // Vision cone: two boundary rays and a connecting arc.
        float facingAngle = facingDirection.angleDeg();
        float halfAngle = VISION_HALF_ANGLE_DEGREES;

        float leftAngle = facingAngle + halfAngle;
        float rightAngle = facingAngle - halfAngle;

        float leftX = x + VISION_RANGE
            * (float) Math.cos(Math.toRadians(leftAngle));
        float leftY = y + VISION_RANGE
            * (float) Math.sin(Math.toRadians(leftAngle));

        float rightX = x + VISION_RANGE
            * (float) Math.cos(Math.toRadians(rightAngle));
        float rightY = y + VISION_RANGE
            * (float) Math.sin(Math.toRadians(rightAngle));

        shapes.setColor(1f, 0.8f, 0f, 1f);

        shapes.line(x, y, leftX, leftY);
        shapes.line(x, y, rightX, rightY);

        shapes.arc(
            x, y,
            VISION_RANGE,
            rightAngle,
            halfAngle * 2f,
            32
        );

        shapes.setColor(1f, 0.2f, 0.2f, 1f); // red separation circle
        shapes.circle(x, y, (state == GooseState.SWARMING ? SEPARATION_RADIUS_STANDING : SEPARATION_RADIUS_RUNNING) / 2f, 24); // CHANGED: ...two geese's circles touch when they are the separation distance apart (depends on state)
        shapes.setColor(0.2f, 1f, 0.2f, 1f); // green honk ring around the player
        shapes.circle(playerCentre.x, playerCentre.y, HONK_RANGE, 64); // where swarming geese should stand

        // Draws line from distracted goose to bread
        if (bread != null) {
            bread.getCentre(breadCentre);
            shapes.setColor(1f, 0.5f, 0f, 1f);
            shapes.line(x, y, breadCentre.x, breadCentre.y);
        }

        // Facing direction: short white line.
        shapes.setColor(1f, 1f, 1f, 1f);

        shapes.line(
            x, y,
            x + facingDirection.x * 25f,
            y + facingDirection.y * 25f
        );
    }
}
