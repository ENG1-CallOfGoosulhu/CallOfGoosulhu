package io.github.goldengoose.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.assets.CogFonts;
import io.github.goldengoose.util.Utilities;

/**
 * The tutorial book, opened with the ? button on the main menu and pause menu. Two pages covering
 * controls, the goal, mechanics and things to watch out for. Switch pages with the arrow keys.
 */
public class TutMenu {
    private static final float SCALE_X = 6f;
    private static final float SCALE_Y = 4.5f;
    private static final float LEFT_PAGE_X = 12f;
    private static final float PAGE_X = 51f;
    private static final float PAGE_Y = 18f;
    private static final float PAGE_WIDTH = 28f;
    private static final float PAGE_HEIGHT = 46f;
    private static final float PAGE_PADDING = 2f;
    private static final Color INK = new Color(0.23f, 0.15f, 0.2f, 1f);
    private static final Color INK_FADED = new Color(0.23f, 0.15f, 0.2f, 0.4f);

    private final BitmapFont titleFont = Utilities.createUiFont(CogFonts.UNCIALANTIQUA, 22);
    private final BitmapFont bodyFont = Utilities.createUiFont(CogFonts.UNCIALANTIQUA, 12);
    private final BitmapFont arrowFont = Utilities.createUiFont(CogFonts.UNCIALANTIQUA, 20);
    private final GlyphLayout layout = new GlyphLayout();

    private final Texture texture;
    private boolean open = false;
    private int currentPage = 0;
    private static final int MAX_PAGES = 2;

    public TutMenu(Texture texture) {
        this.texture = texture;
    }

    public void setOpen(boolean open) {
        this.open = open;
        if (open) currentPage = 0;
    }
    public boolean isOpen() { return open; }

    public void update() {
        if (!open) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && currentPage < MAX_PAGES - 1) {
            currentPage++;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && currentPage > 0) {
            currentPage--;
        }
    }

    public void draw(SpriteBatch batch, Viewport hud) {
        if (!open) return;

        update();

        float screenWidth = hud.getWorldWidth();
        float screenHeight = hud.getWorldHeight();

        float width = texture.getWidth() * SCALE_X;
        float height = texture.getHeight() * SCALE_Y;

        float x = (screenWidth - width) / 2f;
        float y = (screenHeight - height) / 2f;

        batch.draw(texture, x, y, width, height);

        float pageWidth = (PAGE_WIDTH - PAGE_PADDING * 2) * SCALE_X;
        float pageTop = y + (PAGE_Y + PAGE_HEIGHT - PAGE_PADDING) * SCALE_Y;
        float pageBottom = y + (PAGE_Y + PAGE_PADDING) * SCALE_Y;

        float leftPageX = x + (LEFT_PAGE_X + PAGE_PADDING) * SCALE_X;
        float rightPageX = x + (PAGE_X + PAGE_PADDING) * SCALE_X;

        if (currentPage == 0) {
            layout.setText(titleFont, "Controls", INK, pageWidth, Align.center, true);
            titleFont.draw(batch, layout, leftPageX, pageTop);
            float leftBodyTop = pageTop - layout.height - 6f;

            String controls = "WASD - Movement\n\nT - Open Tome\n\nArrows - Turn pages";            layout.setText(bodyFont, controls, INK, pageWidth, Align.left, true);
            bodyFont.draw(batch, layout, leftPageX, (leftBodyTop + pageBottom + layout.height) / 2f);

            layout.setText(titleFont, "Goal", INK, pageWidth, Align.center, true);
            titleFont.draw(batch, layout, rightPageX, pageTop);
            float rightBodyTop = pageTop - layout.height - 6f;

            String goal = "Find a way out, dispersing geese on your way.";
            layout.setText(bodyFont, goal, INK, pageWidth, Align.left, true);
            bodyFont.draw(batch, layout, rightPageX, (rightBodyTop + pageBottom + layout.height) / 2f);

            arrowFont.setColor(INK);
            arrowFont.draw(batch, ">>>", x + width - 5f * SCALE_X - pageWidth, y + 10f * SCALE_Y, pageWidth, Align.right, false);

        } else if (currentPage == 1) {
            layout.setText(titleFont, "Mechanics", INK, pageWidth, Align.center, true);
            titleFont.draw(batch, layout, leftPageX, pageTop);
            float leftBodyTop = pageTop - layout.height - 6f;

            String mechanics = "Tall Grass:\nHides you from geese\n\nMud Puddles:\nSlow you down";
            layout.setText(bodyFont, mechanics, INK, pageWidth, Align.left, true);
            bodyFont.draw(batch, layout, leftPageX, (leftBodyTop + pageBottom + layout.height) / 2f);

            layout.setText(titleFont, "Watch Out!", INK, pageWidth, Align.center, true);
            titleFont.draw(batch, layout, rightPageX, pageTop);
            float rightBodyTop = pageTop - layout.height - 6f;

            String warnings = "Patience:\nDrops near geese.\nIf it runs out you become a goose!\n\nTimer:\nYou have 5 minutes to escape!";
            layout.setText(bodyFont, warnings, INK, pageWidth, Align.left, true);
            bodyFont.draw(batch, layout, rightPageX, (rightBodyTop + pageBottom + layout.height) / 2f);

            arrowFont.setColor(INK);
            arrowFont.draw(batch, "<<<", x + 5f * SCALE_X, y + 10f * SCALE_Y, pageWidth, Align.left, false);
        }

        arrowFont.setColor(Color.WHITE);
    }

    public void dispose() {
        titleFont.dispose();
        bodyFont.dispose();
        arrowFont.dispose();
    }
}
