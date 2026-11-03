package io.github.goldengoose.lighting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

/** Adds eerie lovecraftianish lighting to the map (uses the "Lights" and "Roofs" layers from Tiled). (UR_ALTER_CAMPUS) */
public class GameLighting {

    /** One light, from a circle on the Lights layer. */
    private static class Light {
        float x;
        float y;
        float radius;
        float brightness;
        Color colour; // libGDX made us spell it "Color", but this one's ours. We Brits will not concede.
    }

    /** All the lights on the map. */
    private final List<Light> lights = new ArrayList<>();

    /** How dark it is away from the lights. */
    private Color darkness = new Color(0.3f, 0.25f, 0.4f, 1f);

    /** Soft round glow pic, stretched and tinted for each light. */
    private final Texture glow = new Texture("lighting/glow.png");

    /** Pic of the roofs, so lamp light can be covered up there. */
    private TiledMapImageLayer roofs;

    /** Hidden pic we draw the darkness and lights onto, before putting it over the game. */
    private final FrameBuffer lightMap;

    /** The light map as a normal pic we can draw, flipped. */
    private final TextureRegion lightMapPicture;

    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private boolean isLit = false;

    /** Reads the lights and roofs from the map. (UR_ALTER_CAMPUS) */
    public GameLighting(TiledMap map, Viewport viewport) {
        this.viewport = viewport;
        lightMap = new FrameBuffer(Pixmap.Format.RGBA8888, (int) viewport.getWorldWidth(), (int) viewport.getWorldHeight(), false);
        glow.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); // smooth when stretched
        lightMap.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        lightMapPicture = new TextureRegion(lightMap.getColorBufferTexture());
        lightMapPicture.flip(false, true);

        MapLayer litLayer = map.getLayers().get("Lights");
        if (litLayer == null) {
            return;
        }
        isLit = true;
        darkness = litLayer.getProperties().get("darkness", darkness, Color.class);
        roofs = (TiledMapImageLayer) map.getLayers().get("Roofs");

        for (MapObject object : litLayer.getObjects()) {
            if (!(object instanceof EllipseMapObject)) {
                continue;
            }
            Ellipse circle = ((EllipseMapObject) object).getEllipse();
            Light light = new Light();
            light.radius = circle.width / 2;
            light.x = circle.x + light.radius;
            light.y = circle.y + light.radius;
            light.brightness = object.getProperties().get("brightness", 1f, Float.class);
            light.colour = object.getProperties().get("colour", Color.WHITE, Color.class);
            lights.add(light);
        }
    }

    /** Draws the lighting over the map and player. (UR_ALTER_CAMPUS) */
    public void render() {
        if (!isLit) {
            return;
        }
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        batch.setProjectionMatrix(camera.combined);

        lightMap.begin();
        ScreenUtils.clear(darkness);
        batch.begin();

        useAddBlending();
        for (Light light : lights) {
            batch.setColor(light.colour.r, light.colour.g, light.colour.b, light.brightness);
            batch.draw(glow, light.x - light.radius, light.y - light.radius, light.radius * 2, light.radius * 2);
        }

        if (roofs != null) {
            useNormalBlending();
            batch.setColor(darkness);
            batch.draw(roofs.getTextureRegion(), roofs.getX(), roofs.getY());
        }
        batch.end();
        lightMap.end();

        viewport.apply();
        float screenLeft = camera.position.x - camera.viewportWidth / 2;
        float screenBottom = camera.position.y - camera.viewportHeight / 2;
        useMultiplyBlending();
        batch.setColor(Color.WHITE);
        batch.begin();
        batch.draw(lightMapPicture, screenLeft, screenBottom, camera.viewportWidth, camera.viewportHeight);
        batch.end();
        useNormalBlending();
    }

    /** Overlapping things add together and get brighter. */
    private void useAddBlending() {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    }

    /** Multiplies with what's underneath: dark makes it dark, white leaves it alone etc. */
    private void useMultiplyBlending() {
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
    }

    /** Normal drawing. */
    private void useNormalBlending() {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Frees up memory when the game closes. */
    public void dispose() {
        glow.dispose();
        batch.dispose();
        lightMap.dispose();
    }
}
