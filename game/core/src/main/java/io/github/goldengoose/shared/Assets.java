package io.github.goldengoose.shared;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class Assets {
    public static final String GOOSE_WALK = "cthulhu_goose_walking.png";
    public static final String GOOSE_RUN = "cthulhu_goose_running.png";
    public static final String PLAYER = "test_player.png";
    public static final String PENTAGRAM = "pentagram.png";
    public static final String PENTAGRAM_SPELL = "pentagram_spell.png";
    public static final String TEST_SPELL = "test_spell.png";
    public static final String SPELL_TOME = "spell_tome_w_writing.png";

    private final AssetManager manager = new AssetManager();

    public void load() {
        manager.load(GOOSE_WALK, Texture.class);
        manager.load(GOOSE_RUN, Texture.class);
        manager.load(PLAYER, Texture.class);
        manager.load(PENTAGRAM, Texture.class);
        manager.load(PENTAGRAM_SPELL, Texture.class);
        manager.load(TEST_SPELL, Texture.class);
        manager.load(SPELL_TOME, Texture.class);
        manager.finishLoading();
    }

    public Texture getTexture(String path) {
        return manager.get(path, Texture.class);
    }

    public void dispose() {
        manager.dispose();
    }
}
