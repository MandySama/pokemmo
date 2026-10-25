package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.server.game.GameService.Follower;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/** GdxFollowerParticleRenderer: 0.75 sprite scale, direction frames and a 150+300ms spawn fade. */
public final class FollowerPokemon implements AutoCloseable {
    private final ClientResources resources;
    private final PlayerMotion owner;
    private final WorldMaps maps;
    private final FollowerMotion motion = new FollowerMotion();
    private final List<Texture> frames = new ArrayList<>();
    private int groundPadding, headPadding;
    private final List<Texture> sparkles = new ArrayList<>();
    private Follower selected = Follower.none();
    private double elapsed, lastMoved, recallStarted = -1;
    private float recallOpacity;
    private final int animationOffset =
            java.util.concurrent.ThreadLocalRandom.current().nextInt(332);
    private boolean suppressed;
    private Follower pending;
    private Consumer<String> pendingError;
    private int[][] directions;
    private float[] offsets = new float[4];

    public FollowerPokemon(ClientResources resources, PlayerMotion owner, WorldMaps maps) {
        this.resources = resources;
        this.owner = owner;
        this.maps = maps;
    }

    public void select(Follower value, Consumer<String> error) {
        if (pending == null && selected.equals(value) || value.equals(pending)) return;
        if (!frames.isEmpty()) {
            if (pending == null) {
                recallOpacity = opacity();
                recallStarted = elapsed;
            }
            pending = value;
            pendingError = error;
            return;
        }
        load(value, error);
    }

    private void load(Follower value, Consumer<String> error) {
        close();
        selected = value;
        elapsed = lastMoved = 0;
        if (value.species() == 0) return;
        try {
            var source = resources.followerSprites();
            if (!source.available(value)) {
                error.accept(resources.strings().get(6910));
                return;
            }
            directions = source.directions(value);
            offsets = source.adjustments(value);
            var images = source.frames(value);
            // Keep one fixed frame origin, as in GdxFollowerParticleRenderer. Recentring
            // each pose on its opaque pixels turns the walking animation into vertical jitter.
            groundPadding = Integer.MAX_VALUE;
            headPadding = Integer.MAX_VALUE;
            try {
                for (var image : images) {
                    int bottom = -1, top = image.getHeight();
                    for (int py = 0; py < image.getHeight(); py++)
                        for (int px = 0; px < image.getWidth(); px++)
                            if ((image.getPixel(px, py) & 255) != 0) {
                                bottom = py;
                                top = Math.min(top, py);
                            }
                    if (bottom >= 0) {
                        groundPadding = Math.min(groundPadding, image.getHeight() - bottom - 1);
                        headPadding = Math.min(headPadding, top);
                    }
                    var texture = new Texture(image);
                    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    frames.add(texture);
                }
            } finally {
                images.forEach(Pixmap::dispose);
            }
            if (groundPadding == Integer.MAX_VALUE) groundPadding = 0;
            if (headPadding == Integer.MAX_VALUE) headPadding = 0;
            if (value.shiny()) loadSparkles();
            var position = owner.movement().position();
            int direction = position.direction();
            int x = position.x() + (direction == 2 ? 1 : direction == 3 ? -1 : 0),
                    y = position.y() + (direction == 0 ? -1 : direction == 1 ? 1 : 0);
            var map = owner.movement().map();
            if (!map.contains(x, y)
                    || (map.block(x, y) & 0xc00) != 0
                    || map.block(x, y) >>> 12 == 15
                    || maps.isWater(maps.tiles(map).behavior(map.block(x, y) & 1023))) {
                x = position.x();
                y = position.y();
            }
            motion.reset(x, y, direction);
        } catch (IOException | RuntimeException failure) {
            close();
            com.badlogic.gdx.Gdx.app.error("follower", "Cannot load follower sprites", failure);
            error.accept(resources.strings().get(6910));
        }
    }

    /** GlBufferStateManager: game.pak textures 3..5 are the original 5x5 star frames. */
    private void loadSparkles() throws IOException {
        try (var input =
                new java.util.zip.GZIPInputStream(resources.apply("sprites/game.pak").read())) {
            byte[] bytes = input.readAllBytes();
            var data = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            if (bytes.length < 9 || (bytes[8] & 255) < 6)
                throw new IOException("Missing follower star frames");
            data.position(9);
            for (int index = 0; index < 6; index++) {
                int length = data.getInt();
                if (length <= 0 || length > data.remaining())
                    throw new IOException("Invalid game sprite image");
                if (index >= 3) {
                    var pixels = new Pixmap(bytes, data.position(), length);
                    try {
                        var texture = new Texture(pixels);
                        texture.setFilter(
                                Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                        sparkles.add(texture);
                    } finally {
                        pixels.dispose();
                    }
                }
                data.position(data.position() + length);
            }
        }
    }

    public void step(PlayerMotion.Step step) {
        motion.step(step);
    }

    public void update(float delta) {
        boolean hidden = !owner.allowsFollower();
        if (suppressed && !hidden) {
            var position = owner.movement().position();
            motion.reset(owner.x(), owner.y(), position.direction());
            elapsed = lastMoved = 0;
        }
        suppressed = hidden;
        if (Float.isFinite(delta) && delta > 0) elapsed += delta;
        if (owner.moving()) lastMoved = elapsed;
        if (pending != null && elapsed - recallStarted >= .3) {
            var value = pending;
            var error = pendingError;
            load(value, error);
        }
    }

    public float x() {
        return motion.x(owner.stepProgress());
    }

    public float y() {
        return motion.y(owner.stepProgress());
    }

    public boolean visible() {
        return owner.allowsFollower()
                && !frames.isEmpty()
                && (Math.abs(x() - owner.x()) + Math.abs(y() - owner.y()) > .01f);
    }

    public int elevation() {
        var map = owner.movement().map();
        int x = Math.round(x()), y = Math.round(y());
        return map.contains(x, y) ? map.block(x, y) >>> 12 : owner.movement().elevation();
    }

    public float opacity() {
        return pending != null
                ? recallOpacity * (float) Math.max(0, 1 - (elapsed - recallStarted) / .3)
                : (float) Math.min(1, Math.max(0, (elapsed - .15) / .3));
    }

    public void draw(SpriteBatch batch, float footX, float footY) {
        if (!visible()) return;
        int direction = motion.direction();
        long animationTime = System.currentTimeMillis() + animationOffset;
        int index = FollowerAnimation.frame(directions[direction], animationTime, frames.size());
        if (index < 0 || index >= frames.size()) return;
        var image = frames.get(index);
        float alpha = opacity();
        int bounce;
        try {
            bounce =
                    Integer.parseInt(
                            resources.preferences().get("client.gameplay.follower_bounce", "0"));
        } catch (NumberFormatException invalid) {
            bounce = 0;
        }
        boolean raised =
                bounce != 2
                        && (bounce == 0 || elapsed - lastMoved <= .3)
                        && FollowerAnimation.bounce(animationTime);
        footY += raised ? 1 : 0;
        footX += offsets[1] * .25f;
        footY -= offsets[2] * .25f;
        batch.setColor(1, 1, 1, alpha);
        float scale =
                .75f * (selected.alpha() ? 1.3333334f : 1f) * (offsets[0] > 0 ? offsets[0] : 1);
        float width = image.getWidth() * scale;
        boolean mirror = FollowerAnimation.mirror(direction, frames.size());
        batch.draw(
                image,
                footX - width / 2 + (mirror ? width : 0),
                footY - groundPadding * scale,
                mirror ? -width : width,
                image.getHeight() * scale);
        if (selected.shiny() && !sparkles.isEmpty()) {
            // GIF reference: alternating sides, a bright-to-small pulse, continuing while idle or
            // walking.
            double tick = elapsed / .175;
            int phase = (int) tick % 4, side = ((int) tick / 4) % 2;
            if (phase < 3) {
                var star = sparkles.get(2 - phase);
                float sx = footX + (side == 0 ? -10 : 8),
                        sy =
                                footY
                                        + (image.getHeight() - groundPadding - headPadding) * scale
                                        - 2
                                        - phase * 1.3f;
                batch.setColor(150 / 255f, 158 / 255f, 239 / 255f, alpha);
                batch.draw(star, Math.round(sx) - 2, Math.round(sy) - 2, 5, 5);
            }
        }
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
    }

    @Override
    public void close() {
        frames.forEach(Texture::dispose);
        frames.clear();
        sparkles.forEach(Texture::dispose);
        sparkles.clear();
        groundPadding = 0;
        pending = null;
        pendingError = null;
        recallStarted = -1;
    }
}
