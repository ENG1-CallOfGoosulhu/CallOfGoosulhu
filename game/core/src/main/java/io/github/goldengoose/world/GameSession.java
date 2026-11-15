package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.GameTimer;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.spells.ElderSignSpell;
import io.github.goldengoose.spells.PentagramTrapSpell;
import io.github.goldengoose.spells.SpellTome;
import io.github.goldengoose.hidden_events.HiddenEventsHandler;
import io.github.goldengoose.audio.AudioManager;
import io.github.goldengoose.util.InputManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores persistent per playthrough info, like player, timer, spell tome, patience, objectives, audio
 * and keycard state. Moves the player between maps when they use a door, and shows the E prompt next
 * to doors.
 */
public class GameSession implements Disposable {
    private static final float DOOR_RANGE = 48f;
    private static final float PROMPT_GAP = -20f;
    private static final float PROMPT_BOB = 2f;
    private static final float PROMPT_BOB_SPEED = 3f;

    private final Assets assets;
    private final Viewport worldViewport;

    private final Player player;
    private final Vector2 playerCentre = new Vector2();
    private final GameTimer timer = new GameTimer(5 * 60);
    private final SpellTome spellTome;
    private final PatienceSystem patienceSystem = new PatienceSystem();
    private final ObjectiveSystem objectiveSystem = new ObjectiveSystem();
    private final AudioManager audio;
    private KeyState keyState = KeyState.NONE;

    private final Map<String, Area> areas = new HashMap<>();
    private Area currentArea;
    private Door overlappingDoor;
    private Door pendingDoor;
    private float pulseTime = 0f;
    private final Vector2 tmp = new Vector2();

    private final Animation<TextureRegion> prompt;

    private boolean debug;

    public GameSession(Assets assets, AudioManager audio, Viewport worldViewport, String startArea, String startSpawn) {
        this.assets = assets;
        this.worldViewport = worldViewport;
        player = new Player(assets.getTexture(Assets.PLAYER));

        spellTome = new SpellTome(assets.getTexture(Assets.SPELL_TOME));
        spellTome.addSpell(new PentagramTrapSpell(this, assets));
        spellTome.addSpell(new ElderSignSpell(this, assets));

        TextureRegion[] keyFrames = TextureRegion.split(assets.getTexture(Assets.INTERACT_KEY), 16, 16)[0];
        prompt = new Animation<>(0.5f, keyFrames);
        prompt.setPlayMode(Animation.PlayMode.LOOP);

        this.audio = audio;

        enter(startArea, startSpawn);
    }

    public void update(float delta) {
        if (pendingDoor != null) {
            enter(pendingDoor.targetArea(), pendingDoor.targetSpawn());
            pendingDoor = null;
        }
        currentArea.getWorld().update(delta);
        currentArea.update(delta);
        spellTome.update(delta);
        audio.setListener(player.position.x + player.getWidth() / 2f, player.position.y + player.getHeight() / 2f);
        audio.update(delta);
        timer.update(delta);
        patienceSystem.update(delta, currentArea.getWorld().getGeese());
        objectiveSystem.update(delta, keyState, currentArea.getName(), player.getPosition());
        pulseTime += delta;
        checkDoors();
    }

    public void draw(SpriteBatch batch) {
        if (overlappingDoor != null) {
            batch.setColor(1f, 1f, 1f, 1f);

            if (prompt != null) {
                TextureRegion icon = prompt.getKeyFrame(pulseTime, true);
                float iw = icon.getRegionWidth();
                float ih = icon.getRegionHeight();
                float bob = (float) Math.sin(pulseTime * PROMPT_BOB_SPEED) * PROMPT_BOB;
                batch.draw(icon, overlappingDoor.bounds().x + overlappingDoor.bounds().width / 2f - iw / 2f, overlappingDoor.bounds().y + PROMPT_GAP + bob, iw, ih);
            }
        }
    }

    public boolean clickDoor(Vector2 worldPos) {
        for (Door door : currentArea.getDoors()) {
            if (!door.bounds().contains(worldPos)) continue;

            if (isInRange(door) && (!door.locked() || keyState == KeyState.MAGNETISED || debug)) {
                pendingDoor = door;
            }
            return true;
        }
        return false;
    }

    private void checkDoors() {
        Rectangle feet = new Rectangle(player.position.x, player.position.y, player.getWidth(), player.getHeight());
        for (Door door : currentArea.getDoors()) {
            if (door.bounds().overlaps(feet) && (!door.locked() || keyState == KeyState.MAGNETISED || debug)) {
                overlappingDoor = door;
                if (InputManager.interact()) {
                    pendingDoor = door;
                }
                return;
            }
        }
        overlappingDoor = null;
    }

    private boolean isInRange(Door door) {
        player.getCentre(playerCentre);
        Rectangle r = door.bounds();
        float nearestX = MathUtils.clamp(playerCentre.x, r.x, r.x + r.width);
        float nearestY = MathUtils.clamp(playerCentre.y, r.y, r.y + r.height);
        return playerCentre.dst2(nearestX, nearestY) <= DOOR_RANGE * DOOR_RANGE;
    }

    private void enter(String areaName, String spawnName) {
        if (currentArea != null) currentArea.getWorld().removePlayer();

        currentArea = areas.computeIfAbsent(areaName, name -> new Area(name, this, assets, worldViewport));
        player.setCollision(currentArea.getCollision());

        currentArea.getWorld().addPlayer();
        currentArea.getSpawn(spawnName, tmp);
        player.setPosition(tmp.x, tmp.y);
        audio.playAreaMusic(areaName);
        player.setAudio(audio);
        player.setInMud(false);
    }

    public Area getArea()         { return currentArea; }
    public GameWorld getWorld()   { return currentArea.getWorld(); }
    public HiddenEventsHandler getEventManager() { return currentArea.getEventManager(); }
    public Player getPlayer()     { return player; }
    public GameTimer getTimer()   { return timer; }
    public SpellTome getSpellTome() { return spellTome; }
    public PatienceSystem getPatienceSystem() { return patienceSystem; }
    public ObjectiveSystem getObjectiveSystem() { return objectiveSystem; }
    public KeyState getKeyState() { return keyState; }
    public void setKeyState(KeyState state) { keyState = state; }
    public AudioManager getAudio() { return audio; }
    public Animation<TextureRegion> getPrompt() { return prompt; }
    public void setDebug(boolean debug) { this.debug = debug; }

    @Override
    public void dispose() {
        for (Area area : areas.values()) {
            area.dispose();
        }
    }
}
