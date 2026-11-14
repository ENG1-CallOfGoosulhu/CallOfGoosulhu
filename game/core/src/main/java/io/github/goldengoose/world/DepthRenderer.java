package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Draws the map's tall objects and the entities in depth order, so whatever is lower on screen is in front. */
public class DepthRenderer {
    private record YSortedTile(TiledMapTile tile, float x, float y, float scaleX, float scaleY, boolean flipX, boolean flipY, float sortY, boolean grassLayer) {}

    // Tiles that are not sorted based on Y
    private final int[] flatLayers;
    // Tiles that are sorted based on Y, duh
    private final List<YSortedTile> ySortedTiles = new ArrayList<>();
    // Grass within this distance of the player is as transparent as it gets
    private static final float GRASS_FADE_INNER = 25f;
    // Grass further away from the player than this is solid;
    private static final float GRASS_FADE_OUTER = 100f;
    // How transparent the grass right next to the player is (1f = solid, 0 = completely invisible)
    private static final float GRASS_ALPHA_MIN = 0.25f;

    // Where the player is standing and how strong the fade is
    private float grassFocusX;
    private float grassFocusY;
    private float grassStrength = 0f;
    private static final int SOLID_ALPHA = 200;

    private final Map<Texture, Pixmap> pixmaps = new HashMap<>();
    private final Map<TiledMapTile, Integer> bottomGaps = new HashMap<>();

    public DepthRenderer(TiledMap map) {
        List<Integer> flat = new ArrayList<>();
        MapLayers layers = map.getLayers();

        for (int i = 0; i < layers.size(); i++) {
            MapLayer layer = layers.get(i);
            boolean ySort = layer.getProperties().get("ySort", false, Boolean.class);
            boolean grassLayer = layer.getProperties().get("tall_grass", false, Boolean.class);

            if (layer instanceof TiledMapTileLayer tiles) {
                if (ySort) addYSortedTiles(tiles, grassLayer);
                else flat.add(i);
            } else if (ySort) {
                addObjects(layer);
            }
        }
        flatLayers = flat.stream().mapToInt(Integer::intValue).toArray();
        ySortedTiles.sort((a, b) -> Float.compare(b.sortY(), a.sortY()));

        for (Pixmap pixmap : pixmaps.values()) {
            pixmap.dispose();
        }
        pixmaps.clear();
    }

    private void addYSortedTiles(TiledMapTileLayer layer, boolean grassLayer) {
        float tw = layer.getTileWidth();
        float th = layer.getTileHeight();

        for (int x = 0; x < layer.getWidth(); x++) {
            int y = 0;
            while (y < layer.getHeight()) {
                if (layer.getCell(x, y) == null) {
                    y++;
                    continue;
                }
                int base = y;
                float sortY = base * th + gapAtBottom(layer, x, base);
                while (y < layer.getHeight() && layer.getCell(x, y) != null) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    ySortedTiles.add(new YSortedTile(cell.getTile(), x * tw, y * th, 1f, 1f,
                        cell.getFlipHorizontally(), cell.getFlipVertically(), sortY, grassLayer));
                    y++;
                }
            }
        }
    }

    private void addObjects(MapLayer layer) {
        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof TiledMapTileMapObject prop)) continue;

            TiledMapTile tile = prop.getTile();
            float sortOffset = tile.getProperties().get("sortOffset", 0f, Float.class);

            ySortedTiles.add(new YSortedTile(tile, prop.getX(), prop.getY(), prop.getScaleX(), prop.getScaleY(),
                prop.isFlipHorizontally(), prop.isFlipVertically(), prop.getY() + sortOffset, false));
        }
    }

    // How transparent grass it drawn
    public void setGrassFade(float focusX, float focusY, float strength){
        grassFocusX = focusX;
        grassFocusY = focusY;
        grassStrength = strength;
    }

    private float grassAlphaFor(float tileCentreX, float tileCentreY){
        float dx = tileCentreX - grassFocusX;
        float dy = tileCentreY - grassFocusY;
        float distance  = (float) Math.sqrt((dx * dx) + (dy * dy));

        float t = (distance - GRASS_FADE_INNER) / (GRASS_FADE_OUTER - GRASS_FADE_INNER);
        t = Math.max(0f, Math.min(1f, t));
        // smooth edges
        t = t * t * (3f - (2f * t));

        float nearAlpha = GRASS_ALPHA_MIN + (1f  - GRASS_ALPHA_MIN) * t;
        return 1f + (nearAlpha - 1f) * grassStrength;
    }
    /** Layer indices to pass to OrthogonalTiledMapRenderer.render(int[]). */
    public int[] getFlatLayers() {
        return flatLayers;
    }

    /** Draws y sorted tiles and entities */
    public void render(SpriteBatch batch, List<Entity> entities, Rectangle view) {
        entities.sort((a, b) -> Float.compare(b.getSortY(), a.getSortY()));

        int next = 0;
        for (YSortedTile tile : ySortedTiles) {
            while (next < entities.size() && entities.get(next).getSortY() >= tile.sortY()) {
                entities.get(next++).render(batch);
            }
            TextureRegion region = tile.tile().getTextureRegion();
            float w = region.getRegionWidth() * tile.scaleX();
            float h = region.getRegionHeight() * tile.scaleY();
            if (isVisible(tile, w, h, view)) {
                drawTile(batch, tile, region, w, h);
            }
        }
        while (next < entities.size()) {
            entities.get(next++).render(batch);
        }
    }

    private boolean isVisible(YSortedTile tile, float w, float h, Rectangle view) {
        return tile.x() + w > view.x && tile.x() < view.x + view.width
            && tile.y() + h > view.y && tile.y() < view.y + view.height;
    }

    private void drawTile(SpriteBatch batch, YSortedTile tile, TextureRegion region, float w, float h) {
        float x = tile.x(), y = tile.y(), width = w, height = h;

        if (tile.flipX()) { x += w; width = -w; }
        if (tile.flipY()) { y += h; height = -h; }

        // Fading the grass - each tile gets its own opacity assigned by how far away it is from the player
        if (tile.grassLayer() && grassStrength > 0f) {
            float alpha = grassAlphaFor(tile.x() + w/ 2f, tile.y() + h/ 2f);
            if(alpha < 1f){
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(region, x, y, width, height);
                batch.setColor(1f, 1f, 1f, 1f);
                return;
            }

        }

        batch.draw(region, x, y, width, height);
    }

    private int transparentRowsAtBottom(TiledMapTile tile) {
        return bottomGaps.computeIfAbsent(tile, t -> {
            TextureRegion region = t.getTextureRegion();
            Pixmap pixmap = pixmaps.computeIfAbsent(region.getTexture(), texture -> {
                TextureData data = texture.getTextureData();
                if (!data.isPrepared()) data.prepare();
                return data.consumePixmap();
            });

            int x0 = region.getRegionX(), y0 = region.getRegionY();
            int w = region.getRegionWidth(), h = region.getRegionHeight();

            for (int row = h - 1; row >= 0; row--) {
                for (int col = 0; col < w; col++) {
                    if ((pixmap.getPixel(x0 + col, y0 + row) & 0xFF) >= SOLID_ALPHA) {   // alpha byte
                        return h - 1 - row;
                    }
                }
            }
            return h;
        });
    }

    private float gapAtBottom(TiledMapTileLayer layer, int x, int base) {
        float gap = 0;
        for (int y = base; y < layer.getHeight() && layer.getCell(x, y) != null; y++) {
            TiledMapTile tile = layer.getCell(x, y).getTile();
            int rows = transparentRowsAtBottom(tile);
            gap += rows;
            if (rows < tile.getTextureRegion().getRegionHeight()) break;   // found solid art in this tile
        }
        return gap;
    }
}
