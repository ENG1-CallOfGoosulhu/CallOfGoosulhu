package io.github.goldengoose.world;

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

import java.util.HashMap;
import java.util.Map;

public class GameSession implements Disposable {
    private final Assets assets;
    private final Viewport worldViewport;

    private final Player player;
    private final GameTimer timer = new GameTimer(5 * 60);
    private final SpellTome spellTome;
    private final PatienceSystem patienceSystem = new PatienceSystem();
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
        timer.update(delta);
        checkDoors();
        patienceSystem.update(delta, currentArea.getWorld().getGeese());
    }

    private void checkDoors() {
        Rectangle feet = new Rectangle(player.position.x, player.position.y, player.getWidth(), player.getHeight());
        for (Door door : currentArea.getDoors()) {
            if (door.bounds().overlaps(feet)) {
                pendingDoor = door;
                return;
            }
        }
    }

    private void enter(String areaName, String spawnName) {
        if (currentArea != null) currentArea.getWorld().removePlayer();

        currentArea = areas.computeIfAbsent(areaName, name -> new Area(name, player, assets, worldViewport));

        currentArea.getWorld().addPlayer();
        currentArea.getSpawn(spawnName, tmp);
        player.setPosition(tmp.x, tmp.y);
    }

    public Area getArea()         { return currentArea; }
    public GameWorld getWorld()   { return currentArea.getWorld(); }
    public HiddenEventsHandler getEventManager() { return currentArea.getEventManager(); }
    public Player getPlayer()     { return player; }
    public GameTimer getTimer()   { return timer; }
    public SpellTome getSpellTome() { return spellTome; }
    public PatienceSystem getPatienceSystem() { return patienceSystem; }
    public KeyState getKeyState() { return keyState; }
    public void setKeyState(KeyState state) { keyState = state; }

    @Override
    public void dispose() {
        for (Area area : areas.values()) area.dispose();
    }
}
