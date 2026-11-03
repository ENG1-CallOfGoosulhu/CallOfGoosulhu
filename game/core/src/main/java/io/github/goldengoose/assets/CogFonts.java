package io.github.goldengoose.assets;

public enum CogFonts {
    CRISTOFORO("fonts/Cristoforo.ttf");

    private final String path;
    CogFonts(final String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }
}
