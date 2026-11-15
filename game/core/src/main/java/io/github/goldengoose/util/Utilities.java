package io.github.goldengoose.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import io.github.goldengoose.assets.CogFonts;

/**
 * Helpers used across the game. Makes fonts stay the same size on any monitor, and splits sprite
 * sheets into animations.
 */
public class Utilities {
    private static final float REFERENCE_SCREEN_HEIGHT = 1080f;
    public static final float UI_WIDTH = 640f;
    public static final float UI_HEIGHT = 480f;

    /** How much bigger text needs to be on player's monitor, so it's the same size as on a 1080p screen */
    public static float getTextScale() {
        return Gdx.graphics.getDisplayMode().height / REFERENCE_SCREEN_HEIGHT;
    }

    public static BitmapFont createFont(CogFonts fontPath, int size) {
        return createFont(fontPath, size, 0f);
    }

    public static BitmapFont createFont(CogFonts fontPath, int size, float borderWidth) {
        return createFont(fontPath, size, borderWidth, Color.WHITE, Color.BLACK);
    }

    public static BitmapFont createFont(CogFonts fontPath, int size, float borderWidth,
                                        Color textColor, Color borderColor) {
        return createFont(fontPath, size, borderWidth, textColor, borderColor, 0f, Color.BLACK);
    }

    public static BitmapFont createFont(CogFonts fontPath, int size, float borderWidth,
                                        Color textColor, Color borderColor,
                                        float shadowOffset, Color shadowColor) {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal(fontPath.toString()));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = Math.round(size * getTextScale());
        parameter.color = textColor;
        parameter.borderWidth = borderWidth * getTextScale();
        parameter.borderColor = borderColor;
        parameter.shadowOffsetX = Math.round(shadowOffset * getTextScale());
        parameter.shadowOffsetY = Math.round(shadowOffset * getTextScale());
        parameter.shadowColor = shadowColor;

        BitmapFont font = generator.generateFont(parameter);

        generator.dispose();

        return font;
    }

    public static BitmapFont createUiFont(CogFonts fontPath, float size) {
        return createUiFont(fontPath, size, 0f, Color.WHITE, Color.BLACK);
    }

    public static BitmapFont createUiFont(CogFonts fontPath, float size, float borderWidth,
                                          Color textColor, Color borderColor) {
        float toFontSize = REFERENCE_SCREEN_HEIGHT / UI_HEIGHT;
        BitmapFont font = createFont(fontPath, Math.round(size * toFontSize), borderWidth * toFontSize,
            textColor, borderColor);
        font.getData().setScale(UI_HEIGHT / Gdx.graphics.getDisplayMode().height);
        return smooth(font);
    }

    public static BitmapFont smooth(BitmapFont font) {
        font.setUseIntegerPositions(false);
        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
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
