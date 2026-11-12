package io.github.goldengoose.world;

import com.badlogic.gdx.math.Rectangle;

public record Door(Rectangle bounds, String targetArea, String targetSpawn, boolean locked) {}
