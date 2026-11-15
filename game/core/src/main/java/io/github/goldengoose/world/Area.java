package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
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
import io.github.goldengoose.goose.Bread;
import io.github.goldengoose.hidden_events.BreadDispenser;
import io.github.goldengoose.hidden_events.HiddenEventsHandler;
import io.github.goldengoose.hidden_events.TallGrass;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.goose.Goose;

import java.util.ArrayList;
import java.util.List;

/**
 * UR_CAMPUS_MAP: "The game must take place on a UoY campus-style map with limits to where the player should be able to go"
 *
 * One map (campus, Ron Cooke or Ian Wand) loaded from Tiled, with collisions, doors, lighting,
 * mud, tall grass, hidden events and the portal (in Ian Wand).
 */
public class Area implements Disposable {
    private final String name;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final DepthRenderer depthRenderer;
    private final GameLighting lighting;
    private final GameWorld world;
    private final HiddenEventsHandler eventManager;
    private final TallGrass tallGrass;
    private final TiledMapTileLayer mudLayer;
    private float grassStrength = 0f;
    private static final float GRASS_FADE_SPEED = 4f;

    private final Animation<TextureRegion> prompt;
    private final List<Door> doors = new ArrayList<>();
    private final CollisionMap collision;
    private final Rectangle feet = new Rectangle();
    private Rectangle portal;

    public Area(String name, GameSession session, Assets assets, Viewport viewport) {
        this.name = name;
        map = new TmxMapLoader().load("maps/" + name + ".tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        depthRenderer = new DepthRenderer(map);
        lighting = new GameLighting(map, viewport);
        collision = new CollisionMap(map);
        world = new GameWorld(session, map, collision, assets);
        mudLayer = (TiledMapTileLayer) map.getLayers().get("Mud Puddles");

        eventManager = new HiddenEventsHandler(world);
        tallGrass = new TallGrass(map);
        eventManager.register(tallGrass);
        Goose.setConfusedIcon(new TextureRegion(assets.getTexture(Assets.CONFUSED_ICON)));
        Goose.setAlertIcon(new TextureRegion(assets.getTexture(Assets.ALERT_ICON)));
        Bread.setSprite(new TextureRegion(assets.getTexture(Assets.BREAD)));
        Bread.setSpinAnimation(new Animation<TextureRegion>(0.05f, assets.getAtlas(Assets.BREAD_SPIN).findRegions("bread_spin"), Animation.PlayMode.LOOP));

        prompt = session.getPrompt();

        loadDoors();
        loadMagnetiser();
        loadBreadDispenser(assets);
        loadPortal();
    }

    public void update(float delta) {
        eventManager.update(delta);
        checkMudPuddles();

        float target = tallGrass.getFadeAlpha() < 1f ? 1f : 0f;
        if (grassStrength < target){
            grassStrength = Math.min(target, grassStrength + GRASS_FADE_SPEED * delta);
        } else if (grassStrength > target){
            grassStrength = Math.max(target, grassStrength - GRASS_FADE_SPEED * delta);
        }
    }

    private void checkMudPuddles() {
        Player player = world.getPlayer();
        if (mudLayer == null){
            player.setInMud(false);
            return;
        }

        player.getHitbox(feet);

        int tileX = (int) ((feet.x + feet.width / 2f) / mudLayer.getTileWidth());
        int tileY = (int) ((feet.y + feet.height / 2f) / mudLayer.getTileHeight());

        player.setInMud(mudLayer.getCell(tileX, tileY) != null);
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, Rectangle view) {
        mapRenderer.setView(camera);
        mapRenderer.render(depthRenderer.getFlatLayers());

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        world.renderGround(batch);
        Player player = world.getPlayer();
        depthRenderer.setGrassFade(player.getPosition().x + player.getWidth() / 2f, player.getPosition().y, grassStrength);
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
            boolean locked = p.get("locked", false, Boolean.class);
            doors.add(new Door(rect.getRectangle(), targetMap.replace(".tmx", ""), targetSpawn, locked));
        }
    }

    private void loadMagnetiser() {
        MapLayer layer = map.getLayers().get("Interactables");
        if (layer == null) return;

        MapObject magnetiserObject = layer.getObjects().get("keycard_magnetiser");
        if (magnetiserObject == null) return;
        if (!(magnetiserObject instanceof RectangleMapObject rect)) throw new IllegalStateException("'keycard_magnetiser' is not a Rectangle");

        KeycardMagnetiser magnetiser = new KeycardMagnetiser(world, prompt, rect.getRectangle());
        eventManager.register(magnetiser);
    }

    private void loadBreadDispenser(Assets assets){
        MapLayer layer = map.getLayers().get("Spawns");
        if (layer == null) return;

        MapObject object = layer.getObjects().get("bread_dispenser");
        if (object == null) return;
        if (!(object instanceof RectangleMapObject rect)) throw new IllegalStateException("'bread_dispenser' is not a Rectangle");

        Rectangle r = rect.getRectangle();
        eventManager.register(new BreadDispenser(world, assets.getAtlas(Assets.BREAD_DISPENSER), prompt, r.x, r.y, r.width, r.height));
    }

    private void loadPortal() {
        if (!name.equals("Ian_Wand_Interior_MAIN")) return;

        MapObject portalObject = map.getLayers().get("Interactables").getObjects().get("the_pit");
        portal = ((RectangleMapObject) portalObject).getRectangle();
    }

    public GameWorld getWorld() { return world; }
    public HiddenEventsHandler getEventManager() { return eventManager; }
    public List<Door> getDoors() { return doors; }
    public CollisionMap getCollision() { return collision; }
    public String getName() { return name; }
    public Rectangle getPortal() { return portal; }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        lighting.dispose();
    }
}
