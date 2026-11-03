package io.github.goldengoose.spells;

import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.assets.Assets;

public class TestSpell extends Spell{

    public TestSpell(Player player, Assets assets) {
        super(player, assets);
        texture = assets.getTexture(Assets.TEST_SPELL);
    }

    @Override
    protected void triggerEffect(Vector2 worldPos) {

    }
}
