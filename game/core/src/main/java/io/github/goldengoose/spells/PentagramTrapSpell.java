package io.github.goldengoose.spells;

import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.world.GameWorld;

public class PentagramTrapSpell extends Spell {

    private final GameWorld world;

    public PentagramTrapSpell(Player player, GameWorld world, Assets assets) {
        super(player, assets);
        texture = assets.getTexture(Assets.PENTAGRAM_SPELL);
        name = "Pentagram";
        description = "Inscribe a binding circle on the ground. The first goose to step inside is held until the circle fades.";

        this.world = world;

        range = 120f;
        cooldown = 10f;
    }

    @Override
    protected void triggerEffect(Vector2 worldPos) {
        world.addEntity(new PentagramTrap(worldPos, assets.getTexture(Assets.PENTAGRAM)));
    }
}
