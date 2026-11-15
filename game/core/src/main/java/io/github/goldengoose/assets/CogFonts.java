package io.github.goldengoose.assets;

/**
 * The fonts the game uses, each linked to its file.
 */
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
