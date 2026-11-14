package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class CollisionMap {
    private final List<Rectangle> rects = new ArrayList<>();
    private final List<Polygon> polygons = new ArrayList<>();

    private final Vector2 segA = new Vector2(), segB = new Vector2();

    public CollisionMap(TiledMap map) {
        MapLayer layer = map.getLayers().get("Collision");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject r) rects.add(r.getRectangle());
            else if (object instanceof PolygonMapObject p) polygons.add(p.getPolygon());
        }
    }

    public boolean isBlocked(Rectangle box) {
        for (Rectangle r : rects) {
            if (r.overlaps(box)) return true;
        }
        for (Polygon p : polygons) {
            if (overlaps(p, box)) return true;
        }
        return false;
    }

    // Using this to prevent goose ai from even trying to move into walls
    public boolean blocksLine(float x1, float y1, float x2, float y2) {
        segA.set(x1, y1);
        segB.set(x2, y2);
        for (Rectangle r : rects) {
            if (Intersector.intersectSegmentRectangle(segA, segB, r)) return true;
        }
        for (Polygon p : polygons) {
            if (Intersector.intersectSegmentPolygon(segA, segB, p)) return true;
        }
        return false;
    }

    private boolean overlaps(Polygon polygon, Rectangle box) {
        if (!polygon.getBoundingRectangle().overlaps(box)) return false;
        if (polygon.contains(box.x, box.y)) return true;

        float[] v = polygon.getTransformedVertices();
        for (int i = 0; i < v.length; i += 2) {
            int j = (i + 2) % v.length;

            if (Intersector.intersectSegmentRectangle(v[i], v[i + 1], v[j], v[j + 1], box)) return true;
        }
        return false;
    }

    public void renderDebug(ShapeRenderer shapes) {
        shapes.setColor(Color.RED);
        for (Rectangle r : rects) shapes.rect(r.x, r.y, r.width, r.height);
        for (Polygon p : polygons) shapes.polygon(p.getTransformedVertices());
    }
}
