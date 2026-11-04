package io.github.goldengoose.spells;

import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.assets.Assets;

public class ElderSignSpell extends Spell{

    public ElderSignSpell(Player player, Assets assets) {
        super(player, assets);
        texture = assets.getTexture(Assets.ELDER_SIGN_SPELL);
    }

    @Override
    protected void triggerEffect(Vector2 worldPos) {

    }
}
