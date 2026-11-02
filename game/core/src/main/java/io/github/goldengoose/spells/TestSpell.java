package io.github.goldengoose.spells;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.shared.Assets;

public class TestSpell extends Spell{

    public TestSpell(Player player, Assets assets) {
        super(player, assets);
        texture = new Texture("test_spell.png");
    }

    @Override
    protected void triggerEffect(Vector2 worldPos) {

    }
}
