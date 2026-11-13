package io.github.goldengoose.world;

import com.badlogic.gdx.math.MathUtils;
import io.github.goldengoose.goose.Goose;
import java.util.List;

public class PatienceSystem {

    private static final float RESTORE_PER_SECOND = 0.1f;
    private static final float DRAIN_PER_SECOND = 0.08f;
    private float patience = 1.0f;

    public void update(float delta, List<Goose> geese) {
        boolean anyGooseSwarming = false;

        for (Goose goose : geese) {
            if (goose.isSwarming()) {
                anyGooseSwarming = true;
                break;
            }
        }

        if (anyGooseSwarming) {
            patience -= DRAIN_PER_SECOND * delta;
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
