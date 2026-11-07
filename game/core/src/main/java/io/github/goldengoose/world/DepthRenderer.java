package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Draws the map's tall objects and the entities in depth order, so whatever is lower on screen is in front. */
public class DepthRenderer {
    private record YSortedTile(TiledMapTileLayer.Cell cell, float x, float y, float sortY) {}

    // Tiles that are not sorted based on Y
    private final int[] flatLayers;
    // Tiles that are sorted based on Y, duh
    private final List<YSortedTile> ySortedTiles = new ArrayList<>();

    public DepthRenderer(TiledMap map) {
        List<Integer> flat = new ArrayList<>();
        MapLayers layers = map.getLayers();

        for (int i = 0; i < layers.size(); i++) {
            if (!(layers.get(i) instanceof TiledMapTileLayer tiles)) continue;

            if (tiles.getProperties().get("ySort", false, Boolean.class)) {
                addYSortedTiles(tiles);
            } else {
                flat.add(i);
            }
        }
        flatLayers = flat.stream().mapToInt(Integer::intValue).toArray();
        ySortedTiles.sort((a, b) -> Float.compare(b.sortY(), a.sortY()));
    }

    private void addYSortedTiles(TiledMapTileLayer layer) {
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
                    ySortedTiles.add(new YSortedTile(layer.getCell(x, y), x * tw, y * th, base * th));
                    y++;
                }
            }
        }
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
            // anything standing further back than this prop's base is drawn first
            while (next < entities.size() && entities.get(next).getSortY() >= tile.sortY()) {
                entities.get(next++).render(batch);
            }
            if (isVisible(tile, view)) {
                drawTile(batch, tile);
            }
        }
        while (next < entities.size()) {
            entities.get(next++).render(batch);
        }
    }

    private boolean isVisible(YSortedTile tile, Rectangle view) {
        return tile.x() + 32 > view.x && tile.x() < view.x + view.width
            && tile.y() + 32 > view.y && tile.y() < view.y + view.height;
    }

    private void drawTile(SpriteBatch batch, YSortedTile tile) {
        TextureRegion region = tile.cell().getTile().getTextureRegion();
        float w = region.getRegionWidth();
        float h = region.getRegionHeight();
        float x = tile.x(), y = tile.y(), width = w, height = h;

        if (tile.cell().getFlipHorizontally()) { x += w; width = -w; }
        if (tile.cell().getFlipVertically())   { y += h; height = -h; }

        batch.draw(region, x, y, width, height);
    }
}
