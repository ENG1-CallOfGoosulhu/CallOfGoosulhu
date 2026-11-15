package io.github.goldengoose.spells;

import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.world.GameSession;

/**
 * The Pentagram spell in the tome. Casting it places a PentagramTrap where the player clicks.
 */
public class PentagramTrapSpell extends Spell {

    public PentagramTrapSpell(GameSession session, Assets assets) {
        super(session, assets);

        texture = assets.getTexture(Assets.PENTAGRAM_SPELL);
        name = "Pentagram";
        castSound = Assets.SFX_PENTAGRAM_TRIGGER;
        description = "Inscribe a binding circle on the ground. The first goose to step inside is held until the circle fades.";


        range = 120f;
        cooldown = 10f;
    }

    @Override
    protected void triggerEffect(Vector2 worldPos) {
        session.getWorld().addEntity(new PentagramTrap(worldPos, assets.getTexture(Assets.PENTAGRAM), session.getAudio()));
    }
}
