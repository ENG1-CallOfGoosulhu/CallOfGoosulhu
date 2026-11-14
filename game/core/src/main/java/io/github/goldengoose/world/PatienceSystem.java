package io.github.goldengoose.world;

import com.badlogic.gdx.math.MathUtils;
import io.github.goldengoose.goose.Goose;
import java.util.List;

public class PatienceSystem {

    private static final float RESTORE_PER_SECOND = 0.1f;
    private static final float DRAIN_PER_SECOND = 0.08f;
    private static final float CHASE_DRAIN = 0.4f;

    private float patience = 1.0f;

    public void update(float delta, List<Goose> geese) {
        // Patience scales with number of geese surrounding player
        float pressure = 0f;

        for (Goose goose : geese) {
            if (goose.isSwarming()) {
                pressure += 1f;
            } else if (goose.isChasing()){
                pressure += CHASE_DRAIN;
            }
        }

        if (pressure > 0) {
            patience -= DRAIN_PER_SECOND * pressure * delta;
        } else {
            patience += RESTORE_PER_SECOND * delta;
        }

        patience = MathUtils.clamp(patience, 0.0f, 1.0f);
    }

    public float getPatience() {
        return patience;
    }

    public boolean isTransformed() {
        return patience <= 0.0f;
    }
}
