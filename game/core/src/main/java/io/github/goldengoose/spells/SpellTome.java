package io.github.goldengoose.spells;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

public class SpellTome {
    private static final float REFERENCE_WIDTH = 800f;
    private static final float SCALE_X = 2f;
    private static final float SCALE_Y = 1.5f;
    private static final float PADDING = 20f;

    private static final float ICON_OFFSET_X = 11f;
    private static final float ICON_OFFSET_Y = 19f;

    private static final float BAR_OFFSET_RIGHT = 100f;
    private static final float BAR_OFFSET_Y = 57f;
    private static final float BAR_SIZE = 60f;

    private final Texture texture;
    private final List<Spell> spells = new ArrayList<>();
    private int selectedSpell = 0;

    private boolean visible = true;

    public SpellTome(Texture texture) {
        this.texture = texture;

    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void drawShapes(ShapeRenderer shapes, Viewport hud) {
        if (!visible) return;

        Spell currentSpell = getSelectedSpell();
        if (currentSpell == null) return;

        float screenWidth = hud.getWorldWidth();
        float scale = screenWidth / REFERENCE_WIDTH;

        float barX = screenWidth - BAR_OFFSET_RIGHT * scale;
        float barY = BAR_OFFSET_Y * scale;
        float barSize = BAR_SIZE * scale;

        shapes.setColor(0f, 0f, 0f, 0.5f);
        shapes.rect(barX, barY, barSize, barSize);

        float cooldownPercent = currentSpell.getCooldownPercent();
        if (cooldownPercent > 0f) {
            shapes.setColor(1f, 0f, 0f, 1f);
            shapes.rect(barX, barY, barSize, barSize * cooldownPercent);
        }
    }

    public void drawSprites(SpriteBatch batch, Viewport hud) {
        if (!visible) return;

        float screenWidth = hud.getWorldWidth();
        float scale = screenWidth / REFERENCE_WIDTH;

        float width = texture.getWidth() * scale * SCALE_X;
        float height = texture.getHeight() * scale * SCALE_Y;
        float x = screenWidth - width - PADDING * scale;
        float y = PADDING * scale;

        batch.draw(texture, x, y, width, height);

        Spell currentSpell = getSelectedSpell();
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

    /**
     * Activates the currently selected spell
     * @param worldPos world position where the spell should be cast
     * @return true if spell activated
     */
    public boolean activateSelectedSpell(Vector2 worldPos) {
        Spell spell = getSelectedSpell();

        if (spell == null) return false;

        return spell.activate(worldPos);
    }

    /**
     * Returns the currently selected spell
     * @return the currently selected spell
     */
    public Spell getSelectedSpell() {
        if (spells.isEmpty() || selectedSpell < 0 ||
            selectedSpell >= spells.size()) {
            return null;
        }

        return spells.get(selectedSpell);
    }

    /**
     * Changes the currently selected spell
     * @param i the "ID" of the spell to choose. ID is just the order
     * @return true if spell change happened, false if there was no spell at that id;
     */
    public boolean selectSpell(int i) {
        if (spells.isEmpty()) return false;
        if (i >= spells.size() || i < 0 || spells.get(i) == null) return false;

        selectedSpell = i;
        return true;
    }

    public boolean addSpell(Spell spell) {
        if (spells.contains(spell)) return false;
        spells.add(spell);
        return true;
    }

    public void update(float deltaTime) {
        for (Spell spell : spells) {
            spell.reduceCooldown(deltaTime);
        }
    }

    public InputAdapter getInputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (spells.isEmpty()) return false;

                if (keycode == Input.Keys.RIGHT) {
                    selectSpell((selectedSpell + 1) % spells.size());
                    return true;
                } else if (keycode == Input.Keys.LEFT) {
                    selectSpell(
                        (selectedSpell - 1 + spells.size()) % spells.size()
                    );
                    return true;
                }

                return false;
            }
        };
    }
}
