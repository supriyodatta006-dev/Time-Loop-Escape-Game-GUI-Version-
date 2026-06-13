package game.animations;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimationSystem {

    private final List<Particle>     particles     = new ArrayList<>();
    private final List<FlashEffect>  flashes       = new ArrayList<>();
    private final List<FloatText>    floatTexts    = new ArrayList<>();
    private final Random             rng           = new Random();

    private float glitchIntensity = 0f;
    private float ambientPhase    = 0f;

    public void update(float deltaSeconds) {
        ambientPhase += deltaSeconds;

        particles.removeIf(p -> { p.update(deltaSeconds); return p.isDead(); });
        flashes.removeIf(f ->   { f.update(deltaSeconds); return f.isDead(); });
        floatTexts.removeIf(ft->{ ft.update(deltaSeconds); return ft.isDead(); });

        if (glitchIntensity > 0) glitchIntensity -= deltaSeconds * 0.5f;
        glitchIntensity = Math.max(0, glitchIntensity);
    }

    public void clear() {
        particles.clear();
        flashes.clear();
        floatTexts.clear();
        glitchIntensity = 0f;
    }

    public void draw(Graphics2D g2, int width, int height) {

        drawAmbientParticles(g2, width, height);

        particles.forEach(p -> p.draw(g2));

        floatTexts.forEach(ft -> ft.draw(g2));

        flashes.forEach(f -> f.draw(g2, width, height));

        if (glitchIntensity > 0.01f) drawGlitch(g2, width, height);
    }

    private void drawAmbientParticles(Graphics2D g2, int w, int h) {

        for (int i = 0; i < 6; i++) {
            float seed  = i * 137.5f + ambientPhase * 0.3f;
            float x     = Math.abs((seed * 17.3f) % w);
            float y     = Math.abs((seed * 31.1f + ambientPhase * 25f) % h);
            float alpha = 0.06f + 0.04f * (float) Math.sin(ambientPhase + i);
            int   sz    = 2 + (i % 2);
            g2.setColor(new Color(0, 188, 212, (int)(alpha * 255)));
            g2.fillOval((int) x, (int) y, sz, sz);
        }
    }

    private void drawGlitch(Graphics2D g2, int w, int h) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, glitchIntensity * 0.4f));

        for (int i = 0; i < (int)(glitchIntensity * 8); i++) {
            int y = rng.nextInt(h);
            int lineH = 1 + rng.nextInt(4);
            int shift = (rng.nextInt(20) - 10);
            g2.setColor(rng.nextBoolean()
                    ? new Color(255, 0, 80)
                    : new Color(0, 255, 200));
            g2.copyArea(0, y, w, lineH, shift, 0);
        }
        g2.setComposite(old);
    }

    public void spawnClueCollect(int x, int y) {
        for (int i = 0; i < 20; i++) {
            float angle = rng.nextFloat() * 2 * (float) Math.PI;
            float speed = 40 + rng.nextFloat() * 80;
            particles.add(new Particle(
                    x, y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    new Color(0xFFD54F),
                    0.7f + rng.nextFloat() * 0.5f,
                    3 + rng.nextInt(4)
            ));
        }
        floatTexts.add(new FloatText("+ CLUE!", x, y - 20,
                new Color(0xFFD54F), 1.5f));
    }

    public void spawnLoopReset(int width, int height) {
        glitchIntensity = 1.0f;
        int cx = width / 2, cy = height / 2;
        for (int i = 0; i < 40; i++) {
            float angle = rng.nextFloat() * 2 * (float) Math.PI;
            float speed = 60 + rng.nextFloat() * 120;
            particles.add(new Particle(
                    cx, cy,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    new Color(0xF44336),
                    1.0f + rng.nextFloat(),
                    4 + rng.nextInt(5)
            ));
        }
        flashes.add(new FlashEffect(new Color(255, 0, 0, 160), 0.3f));
        floatTexts.add(new FloatText("LOOP RESET", cx, cy,
                new Color(0xFF5252), 2.0f));
    }

    public void spawnEscape(int cx, int cy) {
        for (int i = 0; i < 60; i++) {
            float angle = i * (float)(Math.PI * 2 / 60);
            float r     = 20 + rng.nextFloat() * 60;
            float speed = 80 + rng.nextFloat() * 100;
            particles.add(new Particle(
                    cx + (int)(Math.cos(angle) * r),
                    cy + (int)(Math.sin(angle) * r),
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    new Color(0x00E676),
                    1.5f + rng.nextFloat(),
                    3 + rng.nextInt(6)
            ));
        }
        flashes.add(new FlashEffect(new Color(0, 230, 118, 120), 0.6f));
        floatTexts.add(new FloatText("ESCAPED!", cx, cy - 30,
                new Color(0x69F0AE), 3.0f));
    }

    public void spawnPickup(int x, int y, Color color) {
        for (int i = 0; i < 10; i++) {
            float angle = rng.nextFloat() * 2 * (float) Math.PI;
            float speed = 30 + rng.nextFloat() * 50;
            particles.add(new Particle(
                    x, y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    color, 0.5f + rng.nextFloat() * 0.3f, 2 + rng.nextInt(3)
            ));
        }
    }

    public void triggerGlitch(float intensity) {
        glitchIntensity = Math.min(1f, glitchIntensity + intensity);
    }

    private static class Particle {
        float x, y, vx, vy;
        Color color;
        float lifetime, maxLifetime;
        int   size;

        Particle(float x, float y, float vx, float vy,
                 Color color, float lifetime, int size) {
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.color = color;
            this.lifetime = this.maxLifetime = lifetime;
            this.size = size;
        }

        void update(float dt) {
            x += vx * dt; y += vy * dt;
            vy += 60 * dt;
            vx *= 0.98f;
            lifetime -= dt;
        }

        void draw(Graphics2D g2) {
            float alpha = Math.max(0, lifetime / maxLifetime);
            g2.setColor(new Color(
                    color.getRed(), color.getGreen(), color.getBlue(),
                    (int)(alpha * 200)));
            int s = (int)(size * alpha);
            if (s > 0) g2.fillOval((int) x - s / 2, (int) y - s / 2, s, s);
        }

        boolean isDead() { return lifetime <= 0; }
    }

    private static class FlashEffect {
        Color color;
        float duration, maxDuration;

        FlashEffect(Color color, float duration) {
            this.color = color;
            this.duration = this.maxDuration = duration;
        }

        void update(float dt) { duration -= dt; }

        void draw(Graphics2D g2, int w, int h) {
            float alpha = Math.max(0, duration / maxDuration);
            g2.setColor(new Color(
                    color.getRed(), color.getGreen(), color.getBlue(),
                    (int)(alpha * color.getAlpha())));
            g2.fillRect(0, 0, w, h);
        }

        boolean isDead() { return duration <= 0; }
    }

    private static class FloatText {
        String text;
        float  x, y;
        Color  color;
        float  lifetime, maxLifetime;

        FloatText(String text, float x, float y, Color color, float lifetime) {
            this.text = text; this.x = x; this.y = y;
            this.color = color;
            this.lifetime = this.maxLifetime = lifetime;
        }

        void update(float dt) {
            y -= 30 * dt;
            lifetime -= dt;
        }

        void draw(Graphics2D g2) {
            float alpha = Math.min(1f, lifetime / (maxLifetime * 0.3f))
                        * Math.min(1f, lifetime / maxLifetime * 3f);
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2.setColor(new Color(
                    color.getRed(), color.getGreen(), color.getBlue(),
                    (int)(Math.max(0, alpha) * 255)));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x - fm.stringWidth(text) / 2f, y);
        }

        boolean isDead() { return lifetime <= 0; }
    }
}
