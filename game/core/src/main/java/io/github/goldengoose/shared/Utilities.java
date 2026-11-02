package io.github.goldengoose.shared;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import io.github.goldengoose.shared.enums.CogFonts;

public class Utilities {
    public static BitmapFont createFont(CogFonts fontPath, int size) {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal(fontPath.toString()));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = size;

        BitmapFont font = generator.generateFont(parameter);

        generator.dispose();

        return font;
    }

    @SuppressWarnings("unchecked")
    public static Animation<TextureRegion>[] createAnimations(Texture sheet, float frameTime, int frameSize) {
        TextureRegion[][] frames = TextureRegion.split(sheet, frameSize, frameSize);
        Animation<TextureRegion>[] animations = new Animation[frames.length];

        for (int row = 0; row < frames.length; row++) {
            animations[row] = new Animation<>(frameTime, frames[row]);
            animations[row].setPlayMode(Animation.PlayMode.LOOP);
        }

        return animations;
    }
}
