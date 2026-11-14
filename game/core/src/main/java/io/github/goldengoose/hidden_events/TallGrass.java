package io.github.goldengoose.hidden_events;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import io.github.goldengoose.world.Entity;
import io.github.goldengoose.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes the player hide in tall grass (For how long?) - 3 seconds like req1 says?
 *
 * Have given every layer of "tall grass" in the map collision objects a new property boolean "tall_grass".
 * Code collects these flags.
 *
 * While the player is hidden:
 *   - the goose AI cannot see or hear the player (needs a hidden flag on Player + a check in GooseAI)
 *   - the grass is drawn semi-transparent (needs support in DepthRenderer, which draws the
 *     ySort layers; this class only says HOW transparent via getFadeAlpha())
 */

public class TallGrass extends HiddenEvent {

    // Layer property that marks a tall grass layer in Tiled
    private static final String GRASS_LAYER_PROPERTY = "tall_grass";
    private static final float TILE_SIZE = 32f;
    // How long the player is able to hide in the tall grass
    private static final float HIDE_DURATION_SECONDS = 30f;
    // Cooldown timer before player can hide again
    private static final float COOLDOWN_SECONDS = 0f;
    private static final float FADED_ALPHA = 0.5f;
    private static final float FEET_OFFSET = 4f;

    private final List<TiledMapTileLayer> grassLayers = new ArrayList<>();

    private boolean hiding = false;
    private float hideTimer = 0f;
    private Player hiddenPlayer;

    public TallGrass(TiledMap map) {
        super("Tall grass",
            "You are hidden in the tall grass",
            0, 0,
            mapWidthInWorldUnits(map), mapHeightInWorldUnits(map),
            COOLDOWN_SECONDS);
        collectGrassLayers(map);
    }


    private static float mapWidthInWorldUnits(TiledMap map) {
        return map.getProperties().get("width", Integer.class) * TILE_SIZE;
    }

    private static float mapHeightInWorldUnits(TiledMap map) {
        return map.getProperties().get("height", Integer.class) * TILE_SIZE;
    }

    private void collectGrassLayers(TiledMap map) {
        for (MapLayer layer : map.getLayers()) {
            boolean marked = layer.getProperties().get(GRASS_LAYER_PROPERTY, false, Boolean.class);
            if (marked && layer instanceof TiledMapTileLayer tileLayer) {
                grassLayers.add(tileLayer);
            }
        }

    }

    // Replace hiddenevents trigger zone with one that copies the grass sprite
    @Override
    public boolean contains(Entity other) {
        float feetX = other.getPosition().x + other.getWidth() / 2f;
        float feetY = other.getPosition().y + FEET_OFFSET;
        return isGrassAt(feetX, feetY);
    }

    // Return true if any grass layer in a tile has this world position in it.
    private boolean isGrassAt(float worldX, float worldY) {
        int tileX = (int) Math.floor(worldX / TILE_SIZE);
        int tileY = (int) Math.floor(worldY / TILE_SIZE);
        for (TiledMapTileLayer layer : grassLayers) {
            TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
            if (cell != null && cell.getTile() != null) {
                return true;
            }
        }
        return false;
    }

    // The player has moved onto a grass tile
    @Override
    protected void onEnter(Entity entity) {
        if (entity instanceof Player player && trigger()) {
            hiding = true;
            hideTimer = HIDE_DURATION_SECONDS;
            hiddenPlayer = player;
            player.beginHiding();
        }
    }

    @Override
    protected void onExit(Entity entity) {
        stopHiding();
    }


    @Override
    protected void onUpdate(float deltaTime) {
        if (!hiding) {
            return;
        }
        hideTimer -= deltaTime;
        if (hideTimer <= 0f) {
            stopHiding();
        }
    }

    private void stopHiding() {
        if (hiding) {
            hiding = false;
            hiddenPlayer.endHiding();
            hiddenPlayer = null;
        }
    }

    public boolean isHiding() {
        return hiding;
    }

    // How transparent DepthRenderer should draw the grass layer right now
    public float getFadeAlpha() {
        return hiding ? FADED_ALPHA : 1f;
    }

    @Override
    public void renderDebug(ShapeRenderer shapes) {
        // TODO: Draw trigger box for each grass tile
    }
}
