package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.goose.Goose;
import io.github.goldengoose.lighting.GameLighting;
import io.github.goldengoose.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameWorld {
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> depthEntities = new ArrayList<>();
    private final List<Entity> pendingEntities = new ArrayList<>();
    private final Player player;

    public GameWorld(Player player, TiledMap map, Assets assets) {
        this.player = player;
        entities.add(player);

        MapLayer spawns = map.getLayers().get("GooseSpawns");
        if (spawns != null) {
            for (MapObject spawn : spawns.getObjects()) {
                float x = spawn.getProperties().get("x", Float.class);
                float y = spawn.getProperties().get("y", Float.class);
                entities.add(new Goose(player, new Vector2(x, y), assets));
            }
        }
    }
    public Player getPlayer() {
        return player;
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
        }
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
