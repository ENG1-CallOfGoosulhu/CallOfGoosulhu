package io.github.goldengoose.spells;

import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.world.GameSession;
import io.github.goldengoose.world.GameWorld;

public class ElderSignSpell extends Spell {

    public ElderSignSpell(GameSession session, Assets assets) {
        super(session, assets);

        texture = assets.getTexture(Assets.ELDER_SIGN_SPELL);
        name = "Elder Sign";
        description = "Inscribe a dispersing circle on the ground. Geese flee the symbol until it fades.";

        range = 120f;
        cooldown = 10f;
    }

    @Override
    protected void triggerEffect(Vector2 worldPos) {
        session.getWorld().addEntity(new ElderSign(worldPos, assets.getTexture(Assets.ELDER_SIGN)));
    }
}
