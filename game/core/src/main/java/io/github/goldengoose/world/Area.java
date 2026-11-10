package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.lighting.GameLighting;
import io.github.goldengoose.player.Player;

import java.util.ArrayList;
import java.util.List;


public class Area implements Disposable {
    private final String name;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final DepthRenderer depthRenderer;
    private final GameLighting lighting;
    private final GameWorld world;
    private final List<Door> doors = new ArrayList<>();
    private Door ignoredDoor;

    public Area(String name, Player player, Assets assets, Viewport viewport) {
        this.name = name;
        map = new TmxMapLoader().load("maps/" + name + ".tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        depthRenderer = new DepthRenderer(map);
        lighting = new GameLighting(map, viewport);
        world = new GameWorld(player, map, assets);
        loadDoors();
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, Rectangle view) {
        mapRenderer.setView(camera);
        mapRenderer.render(depthRenderer.getFlatLayers());

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        world.renderGround(batch);
        depthRenderer.render(batch, world.getDepthEntities(), view);
        batch.end();

        world.emitLights(lighting);
        lighting.render();
    }

    public Vector2 getSpawn(String spawnName, Vector2 out) {
        MapLayer layer = map.getLayers().get("Spawns");
        if (layer == null) throw new IllegalStateException(name + " has no 'Spawns' layer");

        MapObject spawn = layer.getObjects().get(spawnName);
        if (spawn == null) throw new IllegalStateException(name + " has no spawn named '" + spawnName + "'");

        return out.set(spawn.getProperties().get("x", Float.class), spawn.getProperties().get("y", Float.class));
    }

    private void loadDoors() {
        MapLayer layer = map.getLayers().get("Doors");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject rect)) continue;

            MapProperties p = object.getProperties();
            String targetMap = p.get("target_map", String.class);
            String targetSpawn = p.get("target_spawn", String.class);
            if (targetMap == null || targetSpawn == null) {
                throw new IllegalStateException("Door '" + object.getName() + "' in " + name + " needs target_map and target_spawn");
            }
            doors.add(new Door(rect.getRectangle(), targetMap.replace(".tmx", ""), targetSpawn));
        }
    }


    public GameWorld getWorld() {
        return world;
    }

    public List<Door> getDoors() {
        return doors;
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        lighting.dispose();
    }
}
