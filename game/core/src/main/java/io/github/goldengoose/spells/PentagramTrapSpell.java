package io.github.goldengoose.spells;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.shared.Assets;
import io.github.goldengoose.shared.GameWorld;

public class PentagramTrapSpell extends Spell {

    private final GameWorld world;

    public PentagramTrapSpell(Player player, GameWorld world, Assets assets) {
        super(player, assets);

        this.world = world;

        range = 120f;
        cooldown = 10f;
    }

    @Override
    protected void triggerEffect(Vector2 worldPos) {
        world.addEntity(new PentagramTrap(worldPos, assets.getTexture(Assets.PENTAGRAM)));
    }
}
