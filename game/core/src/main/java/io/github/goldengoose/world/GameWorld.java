package io.github.goldengoose.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.goldengoose.assets.Assets;
import io.github.goldengoose.goose.Goose;
import io.github.goldengoose.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameWorld {
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> pendingEntities = new ArrayList<>();
    private final Player player;

    public GameWorld(Assets assets) {
        player = new Player(assets.getTexture(Assets.PLAYER));
        entities.add(player);
        entities.add(new Goose(player, new Vector2(200, 200), assets));
    }

    public Player getPlayer() {
        return player;
    }

    public void addEntity(Entity entity) {
        pendingEntities.add(entity);
    }

    public void update(float deltaTime) {
        for (Entity entity : entities) {
            entity.update(deltaTime);
        }

        for (Entity entity : entities) {
            for (Entity other : entities) {
                if (entity != other) {
                    entity.interact(other);
                }
            }
        }
        entities.removeIf(Entity::isExpired);

        if (!pendingEntities.isEmpty()) {
            entities.addAll(pendingEntities);
            pendingEntities.clear();
            entities.sort(Comparator.comparingInt(Entity::getRenderLayer));
        }
    }

    public void render(SpriteBatch batch) {
        for (Entity entity : entities) {
            entity.render(batch);
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
