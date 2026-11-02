package io.github.goldengoose.spells;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.goldengoose.Cog;
import io.github.goldengoose.shared.InputManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpellTome {

    OrthographicCamera camera;
    private final Stage stage;
    private final Cog game;
    private Texture texture;

    private final List<Spell> spells = new ArrayList<>();
    private int selectedSpell = 0;

    private final ShapeRenderer shapes;

    public SpellTome(
        OrthographicCamera camera,
        Cog game,
        ShapeRenderer shapes
    ) {
        this.camera = camera;
        this.game = game;
        this.shapes = shapes;

        texture = new Texture("spell_tome_w_writing.png");

        stage = new Stage(new ScreenViewport(), game.batch);
    }

    public void draw(SpriteBatch batch) {
        batch.setProjectionMatrix(stage.getCamera().combined);

        float screenWidth = stage.getViewport().getWorldWidth();

        float scale = screenWidth / 800f;

        float width = texture.getWidth() * scale * 2f;
        float height = texture.getHeight() * scale * 1.5f;

        float padding = 20f * scale;

        float x = screenWidth - width - padding;
        float y = padding;

        Spell currentSpell = getSelectedSpell();

        batch.end();
        drawCooldownBar();
        batch.begin();

        batch.draw(texture, x, y, width, height);

        if (currentSpell != null) {
            Texture spellTexture = currentSpell.getTexture();
            if (spellTexture == null) return;

            float spellX = x + 11f * scale * 2f;
            float spellY = y + 19f * scale * 1.5f;

            float spellWidth = spellTexture.getWidth() * scale * 2f;
            float spellHeight = spellTexture.getHeight() * scale * 1.5f;

            batch.draw(
                spellTexture,
                spellX,
                spellY,
                spellWidth,
                spellHeight
            );
        }
    }

    private void drawCooldownBar() {
        Spell currentSpell = getSelectedSpell();

        if (currentSpell == null) return;

        float screenWidth = stage.getViewport().getWorldWidth();

        float scale = screenWidth / 800f;

        float barX = screenWidth - 100f * scale;
        float barY = 57f * scale;
        float barWidth = 60f * scale;
        float barHeight = 60f * scale;

        float cooldownPercent = currentSpell.getCooldownPercent();

        shapes.setProjectionMatrix(stage.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0f, 0f, 0f, 0.5f);
        shapes.rect(barX, barY, barWidth, barHeight);

        if (cooldownPercent > 0f) {
            shapes.setColor(1f, 0f, 0f, 1f);
            shapes.rect(
                barX,
                barY,
                barWidth,
                barHeight * cooldownPercent
            );
        }

        shapes.end();
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
        stage.act(deltaTime);

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

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}
