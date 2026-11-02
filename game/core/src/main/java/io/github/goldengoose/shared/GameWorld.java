package io.github.goldengoose.shared;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.goose.Goose;
import io.github.goldengoose.player.Player;
import io.github.goldengoose.spells.PentagramTrap;

import java.util.ArrayList;
import java.util.List;

public class GameWorld {
    private final List<Entity> entities = new ArrayList<>();
    private Assets assets;

    public GameWorld(Assets assets) {
        this.assets = assets;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void init() {
        entities.add(new Goose((Player) entities.get(0), new Vector2(200, 200), assets));
        for (Entity entity : entities) {
            entity.gameStart();
        }
    }

    public void update(float deltaTime) {
        for (Entity entity : entities) {
            entity.update(deltaTime);
        }

        updateTraps();

        entities.removeIf(entity -> {
            if (entity instanceof PentagramTrap trap) {
                if (trap.isExpired()) {
                    return true;
                }
            }

            return false;
        });
    }

    private void updateTraps() {
        for (Entity entity : entities) {
            if (!(entity instanceof PentagramTrap trap)) {
                continue;
            }

            for (Entity other : entities) {
                if (other instanceof Goose goose) {
                    trap.checkGoose(goose);
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Entity entity : entities) {
            if (entity instanceof PentagramTrap) {
                entity.render(batch);
            }
        }

        for (Entity entity : entities) {
            if (!(entity instanceof PentagramTrap)) {
                entity.render(batch);
            }
        }
    }

    public void renderDebug(ShapeRenderer shapes) {
        for (Entity entity : entities) {
            if (entity instanceof Goose goose) {
                goose.renderDebug(shapes);
            }
        }
    }

    public void dispose() {
        entities.clear();
    }
}
