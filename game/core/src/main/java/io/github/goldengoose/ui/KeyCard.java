package io.github.goldengoose.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import io.github.goldengoose.assets.Assets;

public class KeyCard implements Disposable {
    public static final int WIDTH = 46, HEIGHT = 29;

    private final Texture keyCard;
    private final Texture magnetisedKeyCard;

    public KeyCard(Assets assets) {
        keyCard = assets.getTexture(Assets.KEYCARD);
        magnetisedKeyCard = assets.getTexture(Assets.KEYCARD_MAGNETISED);
    }

    public void draw(SpriteBatch batch, float x, float y, float pixel) {
        batch.draw(keyCard, x, y, WIDTH * pixel, HEIGHT * pixel);
    }

    @Override
    public void dispose() {
        keyCard.dispose();
        magnetisedKeyCard.dispose();
    }
}
