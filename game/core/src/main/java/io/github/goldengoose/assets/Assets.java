package io.github.goldengoose.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class Assets {
    public static final String GOOSE_WALK = "cthulhu_goose_walking.png";
    public static final String GOOSE_RUN = "cthulhu_goose_running.png";
    public static final String PLAYER = "Characters/idle_32x32_2.png";
    public static final String PENTAGRAM = "pentagram_sheet.png";
    public static final String ELDER_SIGN = "elder_sign_sheet.png";
    public static final String PENTAGRAM_SPELL = "pentagram_spell.png";
    public static final String ELDER_SIGN_SPELL = "elder_sign_spell.png";
    public static final String SPELL_TOME = "spell_tome_w_writing.png";
    public static final String SPELL_TOME_EMPTY = "spell_tome.png";

    private final AssetManager manager = new AssetManager();

    public void load() {
        manager.load(GOOSE_WALK, Texture.class);
        manager.load(GOOSE_RUN, Texture.class);
        manager.load(PLAYER, Texture.class);
        manager.load(PENTAGRAM, Texture.class);
        manager.load(ELDER_SIGN, Texture.class);
        manager.load(PENTAGRAM_SPELL, Texture.class);
        manager.load(ELDER_SIGN_SPELL, Texture.class);
        manager.load(SPELL_TOME, Texture.class);
        manager.load(SPELL_TOME_EMPTY, Texture.class);
        manager.finishLoading();
    }

    public Texture getTexture(String path) {
        return manager.get(path, Texture.class);
    }

    public void dispose() {
        manager.dispose();
    }
}
