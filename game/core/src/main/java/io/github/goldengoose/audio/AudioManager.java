package io.github.goldengoose.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import io.github.goldengoose.assets.Assets;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Plays all of the game's sound - Music and effects
 *
 * Sound effects:
 *   play() plays at the same volume wherever the player is - meant for UI, spells, footsteps ect.
 *   playAt() plays audio from a specific location in the world - geese honking ect.
 *
 * Music:
 *   playMusic()/playAreaMusic() fading between tracks
 *
 * If a sound's file path is messed up in assets.Assets then it will be skipped and have a log output
 */

public class AudioManager implements Disposable {
    //------------------------------------------------------------------
    // CONSTANTS
    //------------------------------------------------------------------
    // Overall volume of all sound effects
    private static final float SFX_VOLUME = 1f;
    private static final float MUSIC_VOLUME = 0.5f;

    // Every sound effect gets a little pitch variation so repetition doesn't sound the exact same
    private static final float DEFAULT_PITCH_VARIATION = 0.08f;

    // A sound this close to the player plays at full volume
    private static final float FULL_VOLUME_RADIUS = 48f;
    // and one this far away is silent
    private static final float HEARING_RADIUS = 320f;

    // The same sound will not start twice within this many seconds
    // stops sound effects played on the exact same frame from combining their amplitudes and being incredibly loud
    private static final float MIN_REPEAT_SECONDS = 0.04f;

    // How long the music takes to fade out/in when the area changes
    private static final float MUSIC_FADE_SECONDS = 1.5f;

    // Which music plays in which area
    private static final Map<String, String> AREA_MUSIC = new HashMap<>();
    static {
        AREA_MUSIC.put("Outside_campus_cthulhu_props", Assets.MUSIC_OUTSIDE);
        AREA_MUSIC.put("ian_wand", Assets.MUSIC_IAN_WAND);
        AREA_MUSIC.put("Ron_Cooke_Interior_MAIN", Assets.MUSIC_RON_COOKE);
    }
    //------------------------------------------------------------------
    // END CONSTANTS
    //------------------------------------------------------------------

    // One music track and how loud it currently is
    private static class Track {
        final String path;
        final Music music;
        float level;

        Track(String path, Music music, float level) {
            this.path = path;
            this.music = music;
            this.level = level;
        }
    }

    private final Assets assets;

    // Where the player is. playAt() measures distance from here
    private float listenerX;
    private float listenerY;

    private float time = 0f;
    private final Map<String, Float> lastPlayed = new HashMap<>();
    private final Set<String> warned = new HashSet<>();
    private final Map<String[], Integer> lastVariant = new IdentityHashMap<>();

    private Track current;
    private Track outgoing;

    public AudioManager(Assets assets) {
        this.assets = assets;
    }

    // Telling the audio where the player is so that it can be affected by distance
    public void setListener(float x, float y) {
        listenerX = x;
        listenerY = y;
    }

    // Music fade updates
    public void update(float delta) {
        time += delta;
        float step = delta / MUSIC_FADE_SECONDS;

        if (current != null && current.level < 1f) {
            current.level = Math.min(1f, current.level + step);
            current.music.setVolume(current.level * MUSIC_VOLUME);
        }
        if (outgoing != null) {
            outgoing.level = Math.max(0f, outgoing.level - step);
            outgoing.music.setVolume(outgoing.level * MUSIC_VOLUME);
            if (outgoing.level <= 0f) {
                outgoing.music.stop();
                outgoing = null;
            }
        }
    }

    // ------------------------------------------------------------------
    // Sound effects
    // ------------------------------------------------------------------

    // Plays a sound effect at normal volume
    public void play(String path) {
        play(path, 1f, DEFAULT_PITCH_VARIATION);
    }

    // params:volume and pitch variation
    public void play(String path, float volume, float pitchVariation) {
        if (volume <= 0.001f) {
            return;
        }
        Sound sound = assets.getSound(path);
        if (sound == null) {
            warnMissing(path);
            return;
        }
        if (playedTooRecently(path)) {
            return;
        }
        float pitch = 1f + MathUtils.random(-pitchVariation, pitchVariation);
        sound.play(volume * SFX_VOLUME, pitch, 0f);
    }

    // Plays a sounds effect from a specific position in the world
    public void playAt(String path, float x, float y) {
        playAt(path, x, y, 1f, DEFAULT_PITCH_VARIATION);
    }

    public void playAt(String path, float x, float y, float volume, float pitchVariation) {
        play(path, volume * volumeAt(x, y), pitchVariation);
    }

    // Plays a sound from a list of variants - footsteps, honking ect.
    public void playRandom(String[] variants, float volume, float pitchVariation) {
        String path = pickVariant(variants);
        if (path != null) {
            play(path, volume, pitchVariation);
        }
    }

    // Same as playRandom() just at a specific spot in the world
    public void playRandomAt(String[] variants, float x, float y, float volume, float pitchVariation) {
        playRandom(variants, volume * volumeAt(x, y), pitchVariation);
    }

    private String pickVariant(String[] variants) {
        // Collecting the variants whose files actually loaded
        int[] usable = new int[variants.length];
        int count = 0;
        for (int i = 0; i < variants.length; i++) {
            if (assets.getSound(variants[i]) != null) {
                usable[count++] = i;
            } else {
                warnMissing(variants[i]);
            }
        }
        if (count == 0) {
            return null;
        }

        int last = lastVariant.containsKey(variants) ? lastVariant.get(variants) : -1;
        int pick = usable[MathUtils.random(count - 1)];
        if (count > 1) {
            while (pick == last) {
                pick = usable[MathUtils.random(count - 1)];
            }
        }
        lastVariant.put(variants, pick);
        return variants[pick];
    }

    // playing right next to the player smoothing off to HEARING_RADIUS
    float volumeAt(float x, float y) {
        float dx = x - listenerX;
        float dy = y - listenerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float t = (dist - FULL_VOLUME_RADIUS) / (HEARING_RADIUS - FULL_VOLUME_RADIUS);
        t = Math.max(0f, Math.min(1f, t));

        //squared so it drops off like real sound
        return (1f - t) * (1f - t);
    }

    private boolean playedTooRecently(String path) {
        Float last = lastPlayed.get(path);
        if (last != null && time - last < MIN_REPEAT_SECONDS) {
            return true;
        }
        lastPlayed.put(path, time);
        return false;
    }

    // ------------------------------------------------------------------
    // Music
    // ------------------------------------------------------------------

    //Plays the music for an area (Defied in AREA_MUSIC)
    public void playAreaMusic(String areaName) {
        playMusic(AREA_MUSIC.get(areaName));
    }

    // Fades to this track (null fades out)
    public void playMusic(String path) {
        if (path == null) {
            stopMusic();
            return;
        }

        if (current != null && current.path.equals(path)) {
            return;
        }

        Music music = assets.getMusic(path);
        if (music == null) {
            warnMissing(path);
            stopMusic();
            return;
        }

        // Going back to the track that is still fading out: pick it back up where it was, instead of restarting it
        Track next;
        if (outgoing != null && outgoing.path.equals(path)) {
            next = outgoing;
            outgoing = null;
        } else {
            if (outgoing != null) {
                outgoing.music.stop();
            }
            next = new Track(path, music, 0f);
            music.setLooping(true);
            music.setVolume(0f);
            music.play();
        }

        outgoing = current;
        current = next;
    }

    // Fade the music out
    public void stopMusic() {
        if (current == null) {
            return;
        }
        if (outgoing != null) {
            outgoing.music.stop();
        }
        outgoing = current;
        current = null;
    }

    // Missing file log message
    private void warnMissing(String path) {
        if (warned.add(path)) {
            Gdx.app.log("Audio", "Missing sound file, skipping: " + path);
        }
    }

    @Override
    public void dispose() {
        if (current != null) current.music.stop();
        if (outgoing != null) outgoing.music.stop();
    }
}
