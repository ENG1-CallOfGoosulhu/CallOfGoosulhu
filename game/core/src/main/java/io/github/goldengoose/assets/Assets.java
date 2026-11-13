package io.github.goldengoose.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Assets {
    public static final String GOOSE_WALK = "cthulhu_goose_walking.png";
    public static final String GOOSE_RUN = "cthulhu_goose_running.png";
    public static final String PLAYER = "Characters/Amelia_16x16.png";
    public static final String PENTAGRAM = "pentagram_sheet.png";
    public static final String ELDER_SIGN = "elder_sign_sheet.png";
    public static final String PENTAGRAM_SPELL = "pentagram_spell.png";
    public static final String ELDER_SIGN_SPELL = "elder_sign_spell.png";
    public static final String SPELL_TOME = "spell_tome_w_writing.png";
    public static final String SPELL_TOME_EMPTY = "spell_tome.png";
    public static final String BREAD_DISPENSER = "events/bread_dispenser.atlas";
    public static final String INTERACT_KEY = "events/interact_key.png";
    public static final String BREAD = "events/bread.png";
    public static final String BREAD_SPIN = "events/bread_spin.atlas";
    public static final String HOURGLASS_GLASS = "ui/hourglass_glass.png";
    public static final String HOURGLASS_FRAME = "ui/hourglass_frame.png";
    public static final String PATIENCE_FRAME = "ui/patience_frame.png";
    public static final String PATIENCE_EYES = "ui/patience_eye_sheet.png";
    public static final String KEYCARD = "ui/keycard.png";
    public static final String KEYCARD_MAGNETISED = "ui/keycard_magnetised.png";
    public static final String KEYCARD_ITEM = "keycard_item.png";
    public static final String VIGNETTE_0 = "ui/vignette_0.png";
    public static final String VIGNETTE_1 = "ui/vignette_1.png";
    public static final String VIGNETTE_2 = "ui/vignette_2.png";

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
        manager.load(BREAD_DISPENSER, TextureAtlas.class);
        manager.load(INTERACT_KEY, Texture.class);
        manager.load(BREAD, Texture.class);
        manager.load(BREAD_SPIN, TextureAtlas.class);
        manager.load(HOURGLASS_GLASS, Texture.class);
        manager.load(HOURGLASS_FRAME, Texture.class);
        manager.load(PATIENCE_FRAME, Texture.class);
        manager.load(PATIENCE_EYES, Texture.class);
        manager.load(KEYCARD, Texture.class);
        manager.load(KEYCARD_MAGNETISED, Texture.class);
        manager.load(KEYCARD_ITEM, Texture.class);
        manager.load(VIGNETTE_0, Texture.class);
        manager.load(VIGNETTE_1, Texture.class);
        manager.load(VIGNETTE_2, Texture.class);
        manager.finishLoading();
    }

    public Texture getTexture(String path) {
        return manager.get(path, Texture.class);
    }

    public TextureAtlas getAtlas(String path){
        return manager.get(path, TextureAtlas.class);
    }

    public void dispose() {
        manager.dispose();
    }
}
