package io.github.goldengoose.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.assets.CogFonts;
import io.github.goldengoose.spells.Spell;
import io.github.goldengoose.spells.SpellTome;
import io.github.goldengoose.util.Utilities;

public class TomeMenu {
    private static final float REFERENCE_WIDTH = 800f;
    private static final float SCALE_X = 6f;
    private static final float SCALE_Y = 4.5f;
    private static final float ICON_OFFSET_X = 11f;
    private static final float ICON_OFFSET_Y = 19f;

    private static final float PAGE_X = 51f;
    private static final float PAGE_Y = 18f;
    private static final float PAGE_WIDTH = 28f;
    private static final float PAGE_HEIGHT = 46f;
    private static final float PAGE_PADDING = 2f;
    private static final Color INK = new Color(0.23f, 0.15f, 0.2f, 1f);

    private final BitmapFont titleFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, 22);
    private final BitmapFont bodyFont = Utilities.createFont(CogFonts.UNCIALANTIQUA, 14);
    private final GlyphLayout layout = new GlyphLayout();

    private final Texture texture;
    private final SpellTome spellTome;

    public TomeMenu(Cog game, Texture texture, SpellTome spellTome) {
        this.texture = texture;
        this.spellTome = spellTome;
    }

    public void draw(SpriteBatch batch, Viewport hud) {
        float screenWidth = hud.getWorldWidth();
        float screenHeight = hud.getWorldHeight();
        float scale = screenWidth / REFERENCE_WIDTH;

        float sx = scale * SCALE_X;
        float sy = scale * SCALE_Y;

        float width = texture.getWidth() * sx;
        float height = texture.getHeight() * sy;

        float x = (screenWidth - width) / 2f;
        float y = (screenHeight - height) / 2f;

        batch.draw(texture, x, y, width, height);

        Spell currentSpell = spellTome.getSelectedSpell();
        if (currentSpell == null || currentSpell.getTexture() == null) return;

        // Left page: icon
        Texture spellTexture = currentSpell.getTexture();
        batch.draw(
            spellTexture,
            x + ICON_OFFSET_X * sx,
            y + ICON_OFFSET_Y * sy,
            spellTexture.getWidth() * sx,
            spellTexture.getHeight() * sy
        );

        // Right page: description text
        float pageX = x + (PAGE_X + PAGE_PADDING) * sx;
        float pageTop = y + (PAGE_Y + PAGE_HEIGHT - PAGE_PADDING) * sy;
        float pageWidth = (PAGE_WIDTH - PAGE_PADDING * 2) * sx;

        // Text scaling: gotta stop it from scaling twice.
        float textScale = scale / Utilities.getTextScale();
        titleFont.getData().setScale(textScale);
        bodyFont.getData().setScale(textScale);

        layout.setText(titleFont, currentSpell.getName(), INK, pageWidth, Align.center, true);
        titleFont.draw(batch, layout, pageX, pageTop);

        float bodyTop = pageTop - layout.height - 10f * scale;
        layout.setText(bodyFont, currentSpell.getDescription(), INK, pageWidth, Align.left, true);
        bodyFont.draw(batch, layout, pageX, bodyTop);
    }

    public void dispose() {
        titleFont.dispose();
    }
}
