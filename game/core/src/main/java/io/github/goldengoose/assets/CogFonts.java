package io.github.goldengoose.assets;

public enum CogFonts {
    UNCIALANTIQUA("fonts/UncialAntiqua-Regular.ttf");

    private final String path;
    CogFonts(final String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }
}
