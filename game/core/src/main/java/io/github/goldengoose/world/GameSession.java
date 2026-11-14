package io.github.goldengoose.world;

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

import java.util.HashMap;
import java.util.Map;

public class GameSession implements Disposable {
    private static final float DOOR_RANGE = 48f;

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
    private Door pendingDoor;
    private final Vector2 tmp = new Vector2();

    public GameSession(Assets assets, Viewport worldViewport, String startArea, String startSpawn) {
        this.assets = assets;
        this.worldViewport = worldViewport;
        player = new Player(assets.getTexture(Assets.PLAYER));

        spellTome = new SpellTome(assets.getTexture(Assets.SPELL_TOME));
        spellTome.addSpell(new PentagramTrapSpell(this, assets));
        spellTome.addSpell(new ElderSignSpell(this, assets));

        this.audio = new AudioManager(assets);

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
        objectiveSystem.update(delta, keyState, currentArea.getName(), player.getPosition());    }

    public boolean clickDoor(Vector2 worldPos) {
        for (Door door : currentArea.getDoors()) {
            if (!door.bounds().contains(worldPos)) continue;

            if (isInRange(door) && (!door.locked() || keyState == KeyState.MAGNETISED)) {
                pendingDoor = door;
            }
            return true;
        }
        return false;
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

    @Override
    public void dispose() {
        for (Area area : areas.values()) {
            area.dispose();
            audio.dispose();
        }
    }
}
