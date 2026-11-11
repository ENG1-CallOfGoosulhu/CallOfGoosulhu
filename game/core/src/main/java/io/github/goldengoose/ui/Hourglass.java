package io.github.goldengoose.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.goldengoose.assets.Assets;

/** Hourglass that drains as the timer goes down (UR_GAME_TIMER) */
public class Hourglass implements Disposable {

    public static final int WIDTH = 28, HEIGHT = 50;
    private static final int FLOW_X = 13, NECK_Y = 26;
    private static final float FLOW_SPEED = 6f;

    private static final Color GOO_EDGE = Color.valueOf("245a2c"), GOO = Color.valueOf("46963e");
    private static final Color GOO_TOP = Color.valueOf("94e84f"), GOO_SPARKLE = Color.valueOf("d6ffa0");

    private final Texture glass, frame;
    private final Array<GridPoint2> topGoo = new Array<>(), bottomGoo = new Array<>();
    private final boolean[][] edge = new boolean[HEIGHT][WIDTH];
    private final boolean[][] goo = new boolean[HEIGHT][WIDTH];
    private int glassBottomY;
    private final Pixmap pixmap = new Pixmap(WIDTH, HEIGHT, Pixmap.Format.RGBA8888);
    private final Texture gooTexture = new Texture(pixmap);
    private float clock;

    public Hourglass(Assets assets) {
        glass = assets.getTexture(Assets.HOURGLASS_GLASS);
        frame = assets.getTexture(Assets.HOURGLASS_FRAME);
        loadGoo();
        update(0f, 0f);
    }

    /** fallen goes from 0 (all goo at the top) to 1 (all at the bottom). */
    public void update(float delta, float fallen) {
        clock += delta;
        fillGoo(fallen);

        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        for (int y = 0; y < HEIGHT; y++)
            for (int x = 0; x < WIDTH; x++)
                if (goo[y][x]) pixmap.drawPixel(x, y, Color.rgba8888(gooColour(x, y)));
        if (fallen > 0f && fallen < 1f) drawFlow();
        gooTexture.draw(pixmap, 0, 0);
    }

    public void draw(SpriteBatch batch, float x, float y, float pixel) {
        batch.draw(glass, x, y, WIDTH * pixel, HEIGHT * pixel);
        batch.draw(gooTexture, x, y, WIDTH * pixel, HEIGHT * pixel);
        batch.draw(frame, x, y, WIDTH * pixel, HEIGHT * pixel);
    }

    private void loadGoo() {
        for (String line : Gdx.files.internal("ui/hourglass_goo_order.txt").readString().split("\\R")) {
            String[] parts = line.split(" ");
            GridPoint2 grain = new GridPoint2(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            if (parts[0].equals("top")) topGoo.add(grain);
            if (parts[0].equals("edge")) edge[grain.y][grain.x] = true;
            if (parts[0].equals("bottom")) {
                bottomGoo.add(grain);
                glassBottomY = Math.max(glassBottomY, grain.y);
            }
        }
    }

    private void fillGoo(float fallen) {
        for (boolean[] row : goo) java.util.Arrays.fill(row, false);
        for (int i = (int) (fallen * topGoo.size); i < topGoo.size; i++) addGoo(topGoo.get(i));
        for (int i = 0; i < (int) (fallen * bottomGoo.size); i++) addGoo(bottomGoo.get(i));
    }

    private void addGoo(GridPoint2 grain) {
        goo[grain.y][grain.x] = true;
    }

    private Color gooColour(int x, int y) {
        boolean onTop = y == 0 || !goo[y - 1][x];
        boolean middle = x == FLOW_X || x == FLOW_X + 1;
        if (onTop && middle && y > NECK_Y) return GOO_SPARKLE;
        if (edge[y][x]) return GOO_EDGE;
        if (onTop) return GOO_TOP;
        if (sparkles(x, y)) return GOO_SPARKLE;
        return GOO;
    }

    private boolean sparkles(int x, int y) {
        return (x * 7 + y * 13 + (int) (clock * 2f)) % 41 == 0;
    }

    private void drawFlow() {
        int flow = (int) (clock * FLOW_SPEED);
        for (int y = NECK_Y; y <= glassBottomY && !goo[y][FLOW_X] && !goo[y][FLOW_X + 1]; y++) {
            int step = Math.floorMod(y - flow, 6);
            boolean belowNeck = y > NECK_Y + 2;
            pixmap.drawPixel(FLOW_X, y, Color.rgba8888(step == 0 ? GOO_SPARKLE : GOO_TOP));
            pixmap.drawPixel(FLOW_X + 1, y, Color.rgba8888(GOO));
            if (belowNeck && step == 2) pixmap.drawPixel(FLOW_X - 1, y, Color.rgba8888(GOO));
            if (belowNeck && step == 4) pixmap.drawPixel(FLOW_X + 2, y, Color.rgba8888(GOO_EDGE));
        }
    }

    @Override
    public void dispose() {
        gooTexture.dispose();
        pixmap.dispose();
    }
}
