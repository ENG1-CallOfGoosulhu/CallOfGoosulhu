package io.github.goldengoose.world;

import com.badlogic.gdx.math.Rectangle;

/**
 * Door on the map. Stores it's location, which map and spawn point it leads to, and whether it needs
 * the magnetised keycard.
 */
public record Door(Rectangle bounds, String targetArea, String targetSpawn, boolean locked) {}
