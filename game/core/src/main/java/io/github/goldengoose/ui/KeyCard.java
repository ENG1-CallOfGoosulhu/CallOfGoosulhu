package io.github.goldengoose.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.world.KeyState;

/**
 * HUD icon for the keycard. Appears once the player picks it up and swaps to the magnetised version
 * after the magnetiser.
 */
public class KeyCard implements Disposable {
    public static final int WIDTH = 50, HEIGHT = 33;

    private final Texture keyCard;
    private final Texture magnetisedKeyCard;

    public KeyCard(Assets assets) {
        keyCard = assets.getTexture(Assets.KEYCARD);
        magnetisedKeyCard = assets.getTexture(Assets.KEYCARD_MAGNETISED);
    }

    public void draw(SpriteBatch batch, KeyState state, float x, float y, float pixel) {
        if (state == KeyState.NONE) return;
        Texture texture = state == KeyState.MAGNETISED ? magnetisedKeyCard : keyCard;
        batch.draw(texture, x, y, WIDTH * pixel, HEIGHT * pixel);
    }

    @Override
    public void dispose() {
        keyCard.dispose();
        magnetisedKeyCard.dispose();
    }
}
