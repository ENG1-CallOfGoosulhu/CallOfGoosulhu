package io.github.goldengoose.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Assets {
    //---------------------------------------------------------------------------------//
    // IMAGES
    //---------------------------------------------------------------------------------//
    public static final String GOOSE_WALK = "cthulhu_goose_walking.png";
    public static final String GOOSE_RUN = "cthulhu_goose_running.png";
    public static final String PLAYER = "Characters/Amelia_16x16.png";
    public static final String PENTAGRAM = "pentagram_sheet.png";
    public static final String ELDER_SIGN = "elder_sign_sheet.png";
    public static final String PENTAGRAM_SPELL = "pentagram_spell.png";
    public static final String ELDER_SIGN_SPELL = "elder_sign_spell.png";
    public static final String SPELL_TOME = "spell_tome_w_writing.png";
    public static final String SPELL_TOME_EMPTY = "spell_tome.png";
    public static final String INTERACT_KEY = "events/interact_key.png";
    public static final String BREAD = "events/bread.png";
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


    //---------------------------------------------------------------------------------//
    // ANIMATION
    //---------------------------------------------------------------------------------//
    public static final String BREAD_SPIN = "events/bread_spin.atlas";
    public static final String BREAD_DISPENSER = "events/bread_dispenser.atlas";


    //---------------------------------------------------------------------------------//
    // AUDIO
    //---------------------------------------------------------------------------------//

    // Placeholder file paths
    public static final String SFX_PENTAGRAM_TRIGGER = "audio/sfx/spells/pentagramTrigger.wav";
    public static final String SFX_ELDER_SIGN_SPELL = "audio/sfx/spells/elerSign.wav";
    public static final String SFX_SPELL_FAIL = "audio/sfx/spells/spellFail.wav";
    public static final String SFX_DISPENSER = "audio/sfx/dispenser.wav";
    public static final String SFX_KEYCARD_PICKUP = "audio/sfx/keycard_pickup.wav";
    public static final String SFX_DOOR = "audio/sfx/door.wav";
    public static final String MUSIC_OUTSIDE = "audio/music/outside.wav";
    public static final String MUSIC_IAN_WAND = "audio/music/ian_wand.wav";
    public static final String MUSIC_RON_COOKE = "audio/music/ron_cooke.wav";

    public static final String[] SFX_FOOTSTEPS = {
        "audio/sfx/footsteps/footstep1.wav",
        "audio/sfx/footsteps/footstep2.wav",
        "audio/sfx/footsteps/footstep3.wav",
        "audio/sfx/footsteps/footstep4.wav",
        "audio/sfx/footsteps/footstep5.wav",
        "audio/sfx/footsteps/footstep6.wav"
    };

    public static final String[] SFX_GRASS_RUSTLE = {
        "audio/sfx/bush_rustles/rustle1.wav",
        "audio/sfx/bush_rustles/rustle2.wav",
        "audio/sfx/bush_rustles/rustle3.wav",
        "audio/sfx/bush_rustles/rustle4.wav",
        "audio/sfx/bush_rustles/rustle5.wav",
        "audio/sfx/bush_rustles/rustle6.wav",
        "audio/sfx/bush_rustles/rustle7.wav",
    };

    public static String[] SFX_GOOSE_HONK = {
        "audio/sfx/honk/honk1.wav",
        "audio/sfx/honk/honk2.wav",
        "audio/sfx/honk/honk3.wav",
        "audio/sfx/honk/honk4.wav",
        "audio/sfx/honk/honk5.wav",
        "audio/sfx/honk/honk6.wav",
    };

    private static final String[] SOUNDS = {
        SFX_PENTAGRAM_TRIGGER, SFX_SPELL_FAIL, SFX_ELDER_SIGN_SPELL, SFX_DISPENSER, SFX_KEYCARD_PICKUP, SFX_DOOR
    };

    private static final String[] MUSIC_TRACKS = {
        MUSIC_OUTSIDE, MUSIC_IAN_WAND, MUSIC_RON_COOKE
    };


    public static final String CONFUSED_ICON = "question_mark.png";
    public static final String ALERT_ICON = "honk.png";
    public static final String GOOSE_NORMAL = "cutscene/goose_normal.png";
    public static final String PORTAL = "cutscene/portal.png";
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
        manager.load(CONFUSED_ICON, Texture.class);
        manager.load(ALERT_ICON, Texture.class);
        manager.load(GOOSE_NORMAL, Texture.class);
        manager.load(PORTAL, Texture.class);
        loadAudio();
        manager.finishLoading();
    }

    public void loadAudio() {
        for (String path : SOUNDS) {
            if (Gdx.files.internal(path).exists()) {
                manager.load(path, Sound.class);
            }
        }
        for (String path : MUSIC_TRACKS) {
            if (Gdx.files.internal(path).exists()) {
                manager.load(path, Music.class);
            }
        }
        for (String path : SFX_FOOTSTEPS) {
            if (Gdx.files.internal(path).exists()) {
                manager.load(path, Sound.class);
            }
        }
        for (String path : SFX_GRASS_RUSTLE) {
            if (Gdx.files.internal(path).exists()) {
                manager.load(path, Sound.class);
            }
        }

        for (String path : SFX_GOOSE_HONK) {
            if (Gdx.files.internal(path).exists()) {
                manager.load(path, Sound.class);
            }
        }
    }

    // doesn't return an error if the sound's file path doesn't exist (for placeholder sounds)
    public Sound getSound(String path){
        return manager.isLoaded(path, Sound.class) ? manager.get(path, Sound.class) : null;
    }

    public Music getMusic(String path){
        return manager.isLoaded(path, Music.class) ? manager.get(path, Music.class) : null;
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
