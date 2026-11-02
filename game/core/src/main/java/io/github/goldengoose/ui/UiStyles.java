package io.github.goldengoose.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

public final class UiStyles {
    private UiStyles() {}

    public static Label.LabelStyle label(BitmapFont font) {
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        return style;
    }

    public static TextButton.TextButtonStyle button(BitmapFont font) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.YELLOW;
        style.up = new BaseDrawable();
        style.down = new BaseDrawable();
        style.over = new BaseDrawable();
        return style;
    }
}
