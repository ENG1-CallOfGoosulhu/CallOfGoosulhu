package io.github.goldengoose.shared;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import java.awt.*;

public final class UiStyles {
    private UiStyles() {}

    public static com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle label(BitmapFont font) {
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        return style;
    }

    public static TextButton.TextButtonStyle button(BitmapFont font) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        style.overFontColor = com.badlogic.gdx.graphics.Color.YELLOW;
        style.up = new BaseDrawable();
        style.down = new BaseDrawable();
        style.over = new BaseDrawable();
        return style;
    }
}
