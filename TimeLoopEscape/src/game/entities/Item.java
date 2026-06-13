package game.entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Item {

    public enum ItemType { CLUE, KEY_ITEM, RED_HERRING, TOOL }

    private final String   id;
    private final String   name;
    private final String   description;
    private final ItemType type;
    private final Color    color;

    private int  gridX, gridY;
    private boolean collected = false;
    private float   glowPhase = 0f;
    private final List<ItemParticle> particles = new ArrayList<>();

    public Item(String id, String name, String description,
                ItemType type, int gridX, int gridY) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.type        = type;
        this.gridX       = gridX;
        this.gridY       = gridY;
        this.color       = colorForType(type);
    }

    public void update(float deltaSeconds) {
        glowPhase += deltaSeconds * 2.5f;
        if (!collected && Math.random() < 0.2) {
            particles.add(new ItemParticle(
                (float)(Math.random() * 30 - 15),
                (float)(Math.random() * 10),
                (float)(Math.random() * 0.8f + 0.4f)
            ));
        }
        particles.removeIf(p -> { p.update(deltaSeconds); return p.dead(); });
    }

    public void draw(Graphics2D g2, int tileSize) {
        if (collected) return;

        int px = gridX * tileSize + tileSize / 4;
        int py = gridY * tileSize + tileSize / 4;
        int sz = tileSize / 2;

        float alpha = 0.3f + 0.25f * (float) Math.sin(glowPhase);
        int glowAlpha = (int)(alpha * 255);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), glowAlpha));
        g2.fillOval(px - 6, py - 6, sz + 12, sz + 12);

        int bob = (int)(Math.sin(glowPhase * 0.8f) * 3);

        drawIcon(g2, px, py + bob, sz);

        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(name);
        g2.drawString(name, px + sz / 2 - lw / 2, py + bob + sz + 14);

        for (ItemParticle p : particles) {
            float ratio = p.life / p.maxLife;
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(ratio * 180)));
            int psz = (int)(ratio * 4) + 1;
            g2.fillOval(px + (int)p.dx + sz/2, py + bob + (int)p.dy + sz/2, psz, psz);
        }
    }

    private void drawIcon(Graphics2D g2, int x, int y, int sz) {
        switch (type) {
            case CLUE -> {

                g2.setColor(new Color(0xFFF176));
                g2.fillOval(x, y, sz, sz);
                g2.setColor(new Color(0xF57F17));
                g2.setFont(new Font("SansSerif", Font.BOLD, sz - 8));
                g2.drawString("?", x + sz / 4, y + sz - 6);
            }
            case KEY_ITEM -> {

                g2.setColor(new Color(0xFFD54F));
                g2.fillOval(x, y, sz / 2, sz / 2);
                g2.setColor(new Color(0xFFD54F));
                g2.fillRect(x + sz / 2 - 4, y + sz / 4, sz / 2 + 4, sz / 6);
                g2.fillRect(x + sz - 8, y + sz / 4 + sz / 6, 8, sz / 6);
                g2.setColor(new Color(0xE65100));
                g2.drawOval(x, y, sz / 2, sz / 2);
            }
            case RED_HERRING -> {

                g2.setColor(new Color(0xEF5350));
                g2.fillOval(x, y + sz / 4, (int)(sz * 0.7f), sz / 2);
                int[] tx = { x + (int)(sz * 0.65f), x + sz, x + (int)(sz * 0.65f) };
                int[] ty = { y + sz / 4, y + sz / 2, y + (int)(sz * 0.75f) };
                g2.fillPolygon(tx, ty, 3);
            }
            case TOOL -> {

                g2.setColor(new Color(0x90A4AE));
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + sz / 4, y + sz / 4, x + (sz * 3 / 4), y + (sz * 3 / 4));
                g2.drawOval(x, y, sz / 3, sz / 3);
                g2.setStroke(new BasicStroke(1));
            }
        }
    }

    private static class ItemParticle {
        float dx, dy, life, maxLife;
        ItemParticle(float dx, float dy, float life) {
            this.dx = dx; this.dy = dy; this.life = life; this.maxLife = life;
        }
        void update(float dt) { life -= dt; dy -= dt * 15f; dx += (float)(Math.random() * 2 - 1); }
        boolean dead() { return life <= 0; }
    }

    private static Color colorForType(ItemType type) {
        return switch (type) {
            case CLUE        -> new Color(0xFFF176);
            case KEY_ITEM    -> new Color(0xFFD54F);
            case RED_HERRING -> new Color(0xEF5350);
            case TOOL        -> new Color(0x90A4AE);
        };
    }

    public void collect() { collected = true; }
    public void reset()   { collected = false; }

    public String   getId()          { return id; }
    public String   getName()        { return name; }
    public String   getDescription() { return description; }
    public ItemType getType()        { return type; }
    public int      getGridX()       { return gridX; }
    public int      getGridY()       { return gridY; }
    public boolean  isCollected()    { return collected; }
    public Color    getColor()       { return color; }
}
