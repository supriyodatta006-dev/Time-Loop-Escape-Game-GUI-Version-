package game.entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Player {

    public static final int TILE_SIZE = 64;

    private final String name;
    private int   gridX, gridY;
    private int   pixelX, pixelY;
    private int   targetPixelX, targetPixelY;

    private int       sanity      = 100;
    private Direction facing      = Direction.DOWN;
    private boolean   isMoving    = false;
    private float     moveProgress = 1.0f;

    private float breathPhase  = 0f;
    private float walkPhase    = 0f;
    private float auraPhase    = 0f;
    private float glitchTimer  = 0f;

    private boolean isSprinting = false;
    private float   sprintSanityDrain = 0f;

    private final List<TrailParticle> trail = new ArrayList<>();

    private final List<Item> inventory = new ArrayList<>();

    public Player(String name) {
        this.name = name;
        setGridPosition(1, 1);
    }

    public void setGridPosition(int gx, int gy) {
        gridX = gx; gridY = gy;
        pixelX = targetPixelX = gx * TILE_SIZE;
        pixelY = targetPixelY = gy * TILE_SIZE;
        moveProgress = 1.0f;
        isMoving = false;
    }

    public boolean moveTo(int nx, int ny, Direction dir) {
        if (isMoving) return false;
        facing = dir;
        gridX = nx; gridY = ny;
        targetPixelX = nx * TILE_SIZE;
        targetPixelY = ny * TILE_SIZE;
        moveProgress = 0.0f;
        isMoving = true;
        return true;
    }

    public void update(float dt) {
        breathPhase += dt * 1.4f;
        auraPhase   += dt * 2.6f;

        if (isMoving) {
            float speed = isSprinting ? 15f : 8f;
            walkPhase   += dt * (isSprinting ? 18f : 10f);
            moveProgress = Math.min(1.0f, moveProgress + dt * speed);

            if (isSprinting) {
                sprintSanityDrain += dt;
                if (sprintSanityDrain >= 0.15f) {
                    reduceSanity(1);
                    sprintSanityDrain = 0f;
                }
            }

            if (moveProgress >= 1.0f) {
                isMoving = false;
                pixelX = targetPixelX;
                pixelY = targetPixelY;
            } else {
                pixelX = (int) lerp(pixelX, targetPixelX, moveProgress);
                pixelY = (int) lerp(pixelY, targetPixelY, moveProgress);
            }

            if (Math.random() < 0.6) {
                float vx = (float)(Math.random() * 20 - 10);
                float vy = (float)(Math.random() * 10 - 25);
                trail.add(new TrailParticle(
                        pixelX + TILE_SIZE / 2 + (float)(Math.random() * 10 - 5),
                        pixelY + TILE_SIZE - 8 + (float)(Math.random() * 4 - 2),
                        vx, vy, 0.5f + (float)Math.random() * 0.5f,
                        auraColor(0.8f)));
            }
        } else {
            // Emit ambient particles when standing still, depending on sanity
            int intensity = getSanityLevel().ordinal();
            if (intensity > 0 && Math.random() < 0.1 * intensity) {
                float vx = (float)(Math.random() * 10 - 5);
                float vy = (float)(Math.random() * -15 - 5);
                trail.add(new TrailParticle(
                        pixelX + TILE_SIZE / 2 + (float)(Math.random() * 20 - 10),
                        pixelY + TILE_SIZE - 20 + (float)(Math.random() * 20 - 10),
                        vx, vy, 0.4f + (float)Math.random() * 0.4f,
                        auraColor(0.6f)));
            }
        }

        if (getSanityLevel() == SanityLevel.BREAKING) {
            glitchTimer += dt;
        }

        trail.removeIf(p -> { p.update(dt); return p.dead(); });
    }

    public void draw(Graphics2D g2) {
        Graphics2D gc = (Graphics2D) g2.create();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (TrailParticle p : trail) p.draw(gc);

        int gx = 0, gy = 0;
        if (getSanityLevel() == SanityLevel.BREAKING) {
            gx = (int)(Math.sin(glitchTimer * 23f) * 3);
            gy = (int)(Math.cos(glitchTimer * 17f) * 2);
        }

        int bob = (int)(Math.sin(breathPhase) * 2.5f);

        float shadowScale = isMoving ? 0.85f : 1.0f;
        gc.setColor(new Color(0, 0, 0, 55));
        gc.fillOval(pixelX + 6, pixelY + TILE_SIZE - 10,
                (int)(52 * shadowScale), (int)(14 * shadowScale));

        drawAura(gc, pixelX + gx, pixelY + gy + bob);

        drawBody(gc, pixelX + gx, pixelY + gy + bob);

        gc.dispose();
    }

    private void drawAura(Graphics2D g2, int x, int y) {
        SanityLevel sl = getSanityLevel();
        if (sl == SanityLevel.STABLE) return;

        int rings = sl == SanityLevel.BREAKING ? 4 : sl == SanityLevel.PARANOID ? 3 : 2;
        for (int r = rings; r >= 1; r--) {
            float pulse = (float)(0.4 + 0.35 * Math.sin(auraPhase + r * 0.8f));
            Color ac = auraColor(pulse * (r == 1 ? 0.9f : 0.4f));
            int expand = (rings - r + 1) * 6;
            g2.setColor(ac);
            g2.setStroke(new BasicStroke(r == 1 ? 2f : 1f));
            g2.drawOval(x + 10 - expand, y + 4 - expand,
                    44 + expand * 2, 56 + expand * 2);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private Color auraColor(float alpha) {
        return switch (getSanityLevel()) {
            case STRESSED -> new Color(1f, 0.7f, 0f,  Math.min(1f, alpha));
            case PARANOID -> new Color(1f, 0.2f, 0.2f, Math.min(1f, alpha));
            case BREAKING -> new Color(0.6f, 0f, 1f,  Math.min(1f, alpha));
            default       -> new Color(0f, 0.74f, 0.84f, Math.min(1f, alpha));
        };
    }

    private void drawBody(Graphics2D g2, int x, int y) {
        boolean left  = facing == Direction.LEFT;
        boolean right = facing == Direction.RIGHT;
        boolean up    = facing == Direction.UP;
        boolean side  = left || right;
        int     flip  = left ? -1 : 1;

        float sw = isMoving ? (float) Math.sin(walkPhase) * 7f : 0f;
        drawLeg(g2, x + 22, y + 46, (int)( sw), side, flip);
        drawLeg(g2, x + 36, y + 46, (int)(-sw), side, flip);

        if (!side) {
            drawBackpack(g2, x, y, up);
        }

        drawTorso(g2, x, y, side, flip, up);

        float aw = isMoving ? (float) Math.sin(walkPhase) * 5f : 0f;
        if (!side) {
            drawArm(g2, x + 12,  y + 28,  (int)(-aw));
            drawArm(g2, x + 42,  y + 28,  (int)( aw));
        } else {

            drawArm(g2, x + (flip > 0 ? 40 : 14), y + 28, (int)(aw));
        }

        drawHelmet(g2, x, y, side, flip, up);

        if (!side && !up) {
            float cl = 0.5f + 0.5f * (float) Math.sin(breathPhase * 2);
            Color chest = getSanityLevel() == SanityLevel.STABLE
                    ? new Color(0, 230, 200, (int)(120 + cl * 100))
                    : auraColor(0.7f + cl * 0.3f);
            g2.setColor(chest);
            g2.fillOval(x + 28, y + 34, 8, 8);

            g2.setColor(new Color(chest.getRed(), chest.getGreen(),
                    chest.getBlue(), (int)(40 * cl)));
            g2.fillOval(x + 24, y + 30, 16, 16);
        }
    }

    private void drawLeg(Graphics2D g2, int x, int y, int swing, boolean side, int flip) {
        int bx = x + (side ? flip * swing / 2 : 0);
        int by = y + Math.abs(swing) / 3;

        g2.setColor(new Color(0x1A, 0x23, 0x7E));
        g2.fillRoundRect(bx - 5, by, 10, 14, 4, 4);

        g2.setColor(new Color(0x28, 0x3A, 0x9E));
        g2.fillRoundRect(bx - 4, by + 12, 9, 10, 3, 3);

        g2.setColor(new Color(0x0D, 0x0D, 0x1A));
        g2.fillRoundRect(bx - 5, y + 20, 11, 7, 3, 3);

        g2.setColor(new Color(0x00, 0xBC, 0xD4, 80));
        g2.fillRect(bx - 4, y + 21, 9, 2);
    }

    private void drawArm(Graphics2D g2, int x, int y, int swing) {

        g2.setColor(new Color(0x00, 0x87, 0x9C));
        g2.fillRoundRect(x, y + swing, 10, 12, 4, 4);

        g2.setColor(new Color(0x0D, 0x47, 0x6E));
        g2.fillRoundRect(x - 1, y + 12 + swing / 2, 12, 8, 4, 4);

        g2.setColor(new Color(0x00, 0xE5, 0xFF, 100));
        g2.fillRect(x, y + 18 + swing / 2, 10, 2);
    }

    private void drawTorso(Graphics2D g2, int x, int y, boolean side, int flip, boolean up) {

        GradientPaint bodyGrad = up
                ? new GradientPaint(x+16, y+26, new Color(0x00,0x87,0x9C),
                                    x+48, y+54, new Color(0x00,0x3A,0x52))
                : new GradientPaint(x+16, y+26, new Color(0x00,0x96,0xAA),
                                    x+16, y+54, new Color(0x00,0x3F,0x51));
        g2.setPaint(bodyGrad);
        g2.fillRoundRect(x + 16, y + 26, 32, 24, 8, 8);

        g2.setColor(new Color(0x00, 0x60, 0x70));
        g2.fillRoundRect(x + 18, y + 28, 28, 8, 4, 4);

        if (!side) {
            g2.setColor(new Color(0x00, 0xE5, 0xFF, 60));
            g2.fillRect(x + 20, y + 28, 2, 20);
            g2.fillRect(x + 42, y + 28, 2, 20);
        }

        g2.setColor(new Color(0x0A, 0x0A, 0x1A));
        g2.fillRect(x + 16, y + 46, 32, 4);
        g2.setColor(new Color(0x00, 0xBC, 0xD4, 120));
        g2.fillRect(x + 28, y + 46, 8, 4);
    }

    private void drawBackpack(Graphics2D g2, int x, int y, boolean up) {

        int bx = up ? x + 22 : x + 20;
        g2.setColor(new Color(0x0A, 0x14, 0x2A));
        g2.fillRoundRect(bx, y + 26, 12, 22, 4, 4);

        g2.setColor(new Color(0x00, 0xBC, 0xD4, 60));
        g2.fillRect(bx + 2, y + 30, 8, 2);
        g2.fillRect(bx + 2, y + 35, 8, 2);
        g2.fillRect(bx + 2, y + 40, 8, 2);

        float thrust = 0.4f + 0.4f*(float)Math.sin(breathPhase*3);
        g2.setColor(new Color(0x00, 0xE5, 0xFF, (int)(80*thrust)));
        g2.fillOval(bx + 1, y + 46, 4, 4);
        g2.fillOval(bx + 7, y + 46, 4, 4);
    }

    private void drawHelmet(Graphics2D g2, int x, int y, boolean side, int flip, boolean up) {
        int hx = x + 14, hy = y + 6, hw = 36, hh = 34;

        GradientPaint helmetGrad = new GradientPaint(
                hx, hy,       new Color(0x1A, 0x23, 0x7E),
                hx, hy + hh,  new Color(0x0D, 0x14, 0x4A));
        g2.setPaint(helmetGrad);
        g2.fillOval(hx, hy, hw, hh);

        g2.setColor(new Color(0x00, 0x60, 0x80));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(hx, hy, hw, hh);
        g2.setStroke(new BasicStroke(1f));

        float vglow = 0.6f + 0.4f*(float)Math.sin(breathPhase * 1.5f);
        Color visorBase  = new Color(0x00, 0xD4, 0xFF, (int)(180 * vglow));
        Color visorEdge  = new Color(0x00, 0x40, 0x60, 200);

        if (!side) {

            int vx = hx + 5, vy = hy + 10, vw = hw - 10, vh = 16;
            g2.setColor(visorEdge);
            g2.fillRoundRect(vx - 1, vy - 1, vw + 2, vh + 2, 8, 8);
            GradientPaint visorGrad = new GradientPaint(
                    vx, vy,       visorBase,
                    vx, vy + vh,  new Color(0, 80, 140, 200));
            g2.setPaint(visorGrad);
            g2.fillRoundRect(vx, vy, vw, vh, 8, 8);

            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(vx + 3, vy + 2, vw / 2, 4, 3, 3);

            g2.setColor(new Color(0, 255, 255, 200));
            if (up) {
                g2.fillOval(vx + 5, vy + 4, 5, 5);
                g2.fillOval(vx + vw - 10, vy + 4, 5, 5);
            } else {
                g2.fillOval(vx + 5, vy + 6, 5, 5);
                g2.fillOval(vx + vw - 10, vy + 6, 5, 5);
            }
        } else {

            int vx = flip > 0 ? hx + 14 : hx + 4;
            g2.setColor(visorEdge);
            g2.fillRoundRect(vx, hy + 11, 14, 10, 4, 4);
            GradientPaint sideVisor = new GradientPaint(
                    vx, hy + 11, visorBase,
                    vx + 14, hy + 21, new Color(0, 60, 120, 180));
            g2.setPaint(sideVisor);
            g2.fillRoundRect(vx + 1, hy + 12, 12, 8, 3, 3);
        }

        g2.setColor(new Color(0x00, 0x60, 0x80));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(hx + hw / 2, hy, hx + hw / 2, hy - 8);
        g2.setStroke(new BasicStroke(1f));
        float ab = 0.5f + 0.5f*(float)Math.sin(auraPhase * 2);
        g2.setColor(new Color(0, 255, 200, (int)(160 * ab)));
        g2.fillOval(hx + hw / 2 - 3, hy - 12, 6, 6);

        g2.setColor(new Color(0x00, 0x80, 0xA0, 100));
        g2.setStroke(new BasicStroke(2f));
        g2.drawArc(hx + 4, hy + 2, hw - 8, hh / 2, 20, 140);
        g2.setStroke(new BasicStroke(1f));
    }

    private static class TrailParticle {
        float x, y, vx, vy, life, maxLife;
        Color color;
        TrailParticle(float x, float y, float vx, float vy, float life, Color c) { 
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; 
            this.life = life; this.maxLife = life; this.color = c; 
        }
        void update(float dt) { 
            life -= dt; 
            x += vx * dt;
            y += vy * dt; 
        }
        boolean dead() { return life <= 0; }
        void draw(Graphics2D g2) {
            float ratio = life / maxLife;
            int a = Math.max(0, Math.min(255, (int)(ratio * 150)));
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
            int sz = (int)(ratio * 8);
            g2.fillOval((int)x - sz/2, (int)y - sz/2, sz, sz);
        }
    }

    public void fullReset() {
        setGridPosition(1, 1);
        inventory.clear();
        trail.clear();
        facing = Direction.DOWN;
        sanity = Math.max(0, sanity - 15);
    }

    public void reset() {
        fullReset();
    }

    public void pickUp(Item item)         { inventory.add(item); }
    public boolean hasItem(String id)     { return inventory.stream().anyMatch(i -> i.getId().equals(id)); }
    public boolean useItem(String id)     { return inventory.removeIf(i -> i.getId().equals(id)); }
    public List<Item> getInventory()      { return inventory; }

    public int  getSanity()               { return sanity; }
    public void reduceSanity(int amt)     { sanity = Math.max(0, sanity - amt); }
    public SanityLevel getSanityLevel() {
        if (sanity >= 70) return SanityLevel.STABLE;
        if (sanity >= 40) return SanityLevel.STRESSED;
        if (sanity >= 15) return SanityLevel.PARANOID;
        return SanityLevel.BREAKING;
    }

    private float lerp(float a, float b, float t) {
        t = 1 - (1 - t) * (1 - t);
        return a + (b - a) * t;
    }

    public String    getName()    { return name; }
    public int       getGridX()   { return gridX; }
    public int       getGridY()   { return gridY; }
    public int       getPixelX()  { return pixelX; }
    public int       getPixelY()  { return pixelY; }
    public boolean   isMoving()   { return isMoving; }
    public boolean   isSprinting(){ return isSprinting; }
    public void      setSprinting(boolean s) { isSprinting = s; }
    public Direction getFacing()  { return facing; }

    public enum Direction  { UP, DOWN, LEFT, RIGHT }
    public enum SanityLevel{ STABLE, STRESSED, PARANOID, BREAKING }
}
