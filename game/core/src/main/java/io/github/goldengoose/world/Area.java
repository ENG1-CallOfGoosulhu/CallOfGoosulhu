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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.hidden_events.KeycardMagnetiser;
import io.github.goldengoose.lighting.GameLighting;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.goose.Bread;
import io.github.goldengoose.hidden_events.BreadDispenser;
import io.github.goldengoose.hidden_events.HiddenEventsHandler;
import io.github.goldengoose.hidden_events.TallGrass;

import java.util.ArrayList;
import java.util.List;


public class Area implements Disposable {
    private final String name;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final DepthRenderer depthRenderer;
    private final GameLighting lighting;
    private final GameWorld world;
    private final HiddenEventsHandler eventManager;
    private final TallGrass tallGrass;
    private KeycardMagnetiser magnetiser;

    private final Animation<TextureRegion> prompt;
    private final List<Door> doors = new ArrayList<>();
    private Door ignoredDoor;

    public Area(String name, GameSession session, Assets assets, Viewport viewport) {
        this.name = name;
        map = new TmxMapLoader().load("maps/" + name + ".tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        depthRenderer = new DepthRenderer(map);
        lighting = new GameLighting(map, viewport);
        world = new GameWorld(session, map, assets);

        // Hidden events for this area
        eventManager = new HiddenEventsHandler(world);
        tallGrass = new TallGrass(map);
        eventManager.register(tallGrass);
        Bread.setSprite(new TextureRegion(assets.getTexture(Assets.BREAD)));
        Bread.setSpinAnimation(new Animation<TextureRegion>(0.05f, assets.getAtlas(Assets.BREAD_SPIN).findRegions("bread_spin"), Animation.PlayMode.LOOP));

        TextureRegion[] keyFrames = TextureRegion.split(assets.getTexture(Assets.INTERACT_KEY), 16, 16)[0];
        prompt = new Animation<TextureRegion>(0.5f, keyFrames);
        prompt.setPlayMode(Animation.PlayMode.LOOP);
        if (name.equals("Outside_campus_cthulhu_props")) {
            eventManager.register(new BreadDispenser(world, assets.getAtlas(Assets.BREAD_DISPENSER), prompt, 500, 150, 64, 64));
        }
        loadDoors();
        loadMagnetiser(assets);
    }

    public void update(float delta) {
        eventManager.update(delta);
        checkMudPuddles();
    }

    private void checkMudPuddles() {
        com.badlogic.gdx.maps.tiled.TiledMapTileLayer mudLayer =
            (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Mud Puddles");
        if (mudLayer == null) return;

        float px = world.getPlayer().position.x + world.getPlayer().getWidth() / 2;
        float py = world.getPlayer().position.y + world.getPlayer().getHeight() / 2;

        int tileX = (int) (px / 32);
        int tileY = (int) (py / 32);

        boolean inMud = mudLayer.getCell(tileX, tileY) != null;
        world.getPlayer().setInMud(inMud);
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, Rectangle view) {
        mapRenderer.setView(camera);
        mapRenderer.render(depthRenderer.getFlatLayers());

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        world.renderGround(batch);
        depthRenderer.setGrassAlpha(tallGrass.getFadeAlpha());
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


    private void loadMagnetiser(Assets assets) {
        MapLayer layer = map.getLayers().get("Interactables");
        if (layer == null) return;

        MapObject magnetiserObject = layer.getObjects().get("keycard_magnetiser");
        if (magnetiserObject == null) return;
        if (!(magnetiserObject instanceof RectangleMapObject rect)) throw new IllegalStateException("'keycard_magnetiser' is not a Rectangle");

        magnetiser = new KeycardMagnetiser(world, prompt, rect.getRectangle());
        eventManager.register(magnetiser);
    }

    public GameWorld getWorld() {
        return world;
    }

    public HiddenEventsHandler getEventManager() {
        return eventManager;
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
