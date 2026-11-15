package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.goose.Goose;
import io.github.goldengoose.lighting.GameLighting;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.hidden_events.HiddenEvent;
import io.github.goldengoose.goose.Bread;
import io.github.goldengoose.ui.KeyCard;
import io.github.goldengoose.audio.AudioManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * All the entities in the current map (player, geese, bread, spell circles and pickups). Updates them,
 * lets them interact, and passes them onto drawing and lighting.
 */
public class GameWorld {
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> depthEntities = new ArrayList<>();
    private final List<Entity> pendingEntities = new ArrayList<>();
    private final Player player;
    private final GameSession session;
    private final CollisionMap collision;
    public AudioManager getAudio() { return session.getAudio(); } // NEW

    public GameWorld(GameSession session, TiledMap map, CollisionMap collision, Assets assets) {
        this.session = session;
        this.player = session.getPlayer();
        this.collision = collision;
        entities.add(player);

        MapLayer gooseSpawns = map.getLayers().get("GooseSpawns");
        if (gooseSpawns != null) {
            for (MapObject spawn : gooseSpawns.getObjects()) {
                float x = spawn.getProperties().get("x", Float.class);
                float y = spawn.getProperties().get("y", Float.class);
                Goose goose = new Goose(player, new Vector2(x, y), collision, assets);
                goose.setAudio(session.getAudio());
                entities.add(goose);
            }
        }
        MapLayer spawns = map.getLayers().get("Spawns");
        if (spawns == null) throw new IllegalStateException("No 'Spawns' layer");

        MapObject keyCard = spawns.getObjects().get("keyCard");
        if (keyCard == null) return;
        if (!(keyCard instanceof RectangleMapObject rect)) throw new IllegalStateException("'keyCard' is not a Rectangle");

        KeyCardItem keyCardItem = new KeyCardItem(session, rect.getRectangle(), assets.getTexture(Assets.KEYCARD_ITEM));
        addEntity(keyCardItem);

    }
    public Player getPlayer() {
        return player;
    }
    public KeyState getKeyState() { return session.getKeyState(); }
    public void setKeyState(KeyState state) { session.setKeyState(state); }

    public List<Goose> getGeese() {
        List<Goose> geese = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Goose goose) {
                geese.add(goose);
            }
        }
        return geese;
    }

    public void removePlayer() {
        entities.remove(player);
    }
    public void addPlayer() {
        if (!entities.contains(player)) entities.add(player);
    }

    public void addEntity(Entity entity) {
        pendingEntities.add(entity);
    }

    public void update(float deltaTime) {
        for (Entity entity : entities) {
            entity.update(deltaTime);
        }

        for (Entity entity : entities) {
            for (Entity other : entities) {
                if (entity != other) {
                    entity.interact(other);
                }
            }
        }
        entities.removeIf(Entity::isExpired);

        if (!pendingEntities.isEmpty()) {
            entities.addAll(pendingEntities);
            pendingEntities.clear();
            entities.sort(Comparator.comparingInt(Entity::getRenderLayer));
        }
    }

    public void render(SpriteBatch batch) {
        for (Entity entity : entities) {
            entity.render(batch);

        }

    }

    public void renderGround(SpriteBatch batch) {
        for (Entity entity : entities) {
            if (entity.getRenderLayer() == RenderLayer.GROUND) entity.render(batch);
        }
    }

    public void renderDebug(ShapeRenderer shapes) {
        for (Entity entity : entities) {
            if (entity instanceof Goose goose) {
                goose.renderDebug(shapes);
            }
            if (entity instanceof HiddenEvent event) {
                event.renderDebug(shapes);
            }
            if (entity instanceof Bread bread) {
                bread.renderDebug(shapes);
            }
        }
        collision.renderDebug(shapes);
    }

    public void emitLights(GameLighting lighting) {
        for (Entity entity : entities) {
            entity.emitLight(lighting);
        }
    }

    public List<Entity> getDepthEntities() {
        depthEntities.clear();
        for (Entity entity : entities) {
            if (entity.getRenderLayer() != RenderLayer.GROUND) depthEntities.add(entity);
        }
        return depthEntities;
    }
}
