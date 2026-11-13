package io.github.goldengoose.world;

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
import java.util.List;

/** Draws the map's tall objects and the entities in depth order, so whatever is lower on screen is in front. */
public class DepthRenderer {
    private record YSortedTile(TiledMapTile tile, float x, float y, float scaleX, float scaleY, boolean flipX, boolean flipY, float sortY, boolean grassLayer) {}

    // Tiles that are not sorted based on Y
    private final int[] flatLayers;
    // Tiles that are sorted based on Y, duh
    private final List<YSortedTile> ySortedTiles = new ArrayList<>();
    // How transparent the tall grass tiles are drawn, duh
    private float grassAlpha = 1f;

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
                while (y < layer.getHeight() && layer.getCell(x, y) != null) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    ySortedTiles.add(new YSortedTile(cell.getTile(), x * tw, y * th, 1f, 1f,
                        cell.getFlipHorizontally(), cell.getFlipVertically(), base * th, grassLayer));
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
    public void setGrassAlpha(float alpha){
        grassAlpha = alpha;
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

        // Fading the grass
        if (tile.grassLayer && grassAlpha < 1f) {
            batch.setColor(1f, 1f, 1f, grassAlpha);
            batch.draw(region, x, y, width, height);
            batch.setColor(1f, 1f, 1f, 1f);
            return; // NEW
        }

        batch.draw(region, x, y, width, height);
    }
}
