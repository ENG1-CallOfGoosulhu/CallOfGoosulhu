package io.github.goldengoose.spells;

import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.world.GameWorld;

public class ElderSignSpell extends Spell {

    private final GameWorld world;

    public ElderSignSpell(Player player, GameWorld world, Assets assets) {
        super(player, assets);
        texture = assets.getTexture(Assets.ELDER_SIGN_SPELL);

        this.world = world;

        range = 120f;
        cooldown = 10f;
    }

    @Override
    protected void triggerEffect(Vector2 worldPos) {
        world.addEntity(new ElderSign(worldPos, assets.getTexture(Assets.ELDER_SIGN)));
    }
}
