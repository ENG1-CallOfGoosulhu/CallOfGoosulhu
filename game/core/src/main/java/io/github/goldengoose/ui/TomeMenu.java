package io.github.goldengoose.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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

    private final BitmapFont titleFont = Utilities.createFont(CogFonts.CRISTOFORO, 48);
    private final Table table = new Table();

    private final Texture texture;
    private final SpellTome spellTome;

    public TomeMenu(Cog game, Texture texture, SpellTome spellTome) {
        this.texture = texture;
        this.spellTome = spellTome;

        Label title = new Label("Test description ", UiStyles.label(titleFont));

        table.setFillParent(true);

        table.add(title).padBottom(50).colspan(2);
        table.row();
    }

    public void setVisible(boolean visible) {
        table.setVisible(visible);
    }


    public void draw(SpriteBatch batch, Viewport hud) {
        float screenWidth = hud.getWorldWidth();
        float screenHeight = hud.getWorldHeight();
        float scale = screenWidth / REFERENCE_WIDTH;

        float width = texture.getWidth() * scale * SCALE_X;
        float height = texture.getHeight() * scale * SCALE_Y;

        float x = (screenWidth - width) / 2f;
        float y = (screenHeight - height) / 2f;

        batch.draw(texture, x, y, width, height);

        Spell currentSpell = spellTome.getSelectedSpell();
        if (currentSpell == null || currentSpell.getTexture() == null) return;

        Texture spellTexture = currentSpell.getTexture();
        batch.draw(
            spellTexture,
            x + ICON_OFFSET_X * scale * SCALE_X,
            y + ICON_OFFSET_Y * scale * SCALE_Y,
            spellTexture.getWidth() * scale * SCALE_X,
            spellTexture.getHeight() * scale * SCALE_Y
        );
    }

    public void dispose() {
        titleFont.dispose();
    }
}
