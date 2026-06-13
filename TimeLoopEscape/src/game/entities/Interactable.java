package game.entities;

import java.awt.*;
import java.util.function.Consumer;

public class Interactable {

    public enum InteractableType {
        PUZZLE_PANEL, LOCKED_DOOR, BOOKSHELF,
        CLOCK, MIRROR, CHEST, COMPUTER_TERMINAL
    }

    private final String           id;
    private final String           label;
    private final String           hint;
    private final InteractableType type;
    private final int              gridX, gridY;

    private final String requiredItemId;

    private final Consumer<Interactable> onActivate;

    private boolean activated  = false;
    private float   glowPhase  = 0f;
    private String  floatingMsg = null;
    private float   msgTimer   = 0f;

    public Interactable(String id, String label, String hint,
                        InteractableType type, int gridX, int gridY,
                        String requiredItemId,
                        Consumer<Interactable> onActivate) {
        this.id             = id;
        this.label          = label;
        this.hint           = hint;
        this.type           = type;
        this.gridX          = gridX;
        this.gridY          = gridY;
        this.requiredItemId = requiredItemId;
        this.onActivate     = onActivate;
    }

    public void update(float deltaSeconds) {
        glowPhase += deltaSeconds * 2f;
        if (msgTimer > 0) {
            msgTimer -= deltaSeconds;
            if (msgTimer <= 0) floatingMsg = null;
        }
    }

    public boolean tryActivate(Player player) {
        if (activated) {
            showFloating("Already used.");
            return false;
        }
        if (requiredItemId != null && !player.hasItem(requiredItemId)) {

            showFloating(hint);
            return false;
        }
        activated = true;
        if (onActivate != null) onActivate.accept(this);

        return true;
    }

    public void showFloating(String msg) {
        floatingMsg = msg;
        msgTimer    = 2.5f;
    }

    public void draw(Graphics2D g2, int tileSize) {
        int px = gridX * tileSize;
        int py = gridY * tileSize;

        Color baseColor = activated ? new Color(0x4CAF50) : new Color(0x0288D1);

        float alpha = 0.25f + 0.2f * (float) Math.sin(glowPhase);
        
        for (int i = 0; i < 3; i++) {
            float ringAlpha = Math.max(0, alpha - i * 0.1f);
            if (ringAlpha > 0) {
                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(),
                                      baseColor.getBlue(), (int)(ringAlpha * 255)));
                int offset = 4 - i * 2;
                g2.fillRoundRect(px + offset, py + offset, tileSize - offset * 2, tileSize - offset * 2, 12 + i * 2, 12 + i * 2);
            }
        }

        g2.setColor(activated ? new Color(0x388E3C) : new Color(0x01579B));
        g2.fillRoundRect(px + 8, py + 8, tileSize - 16, tileSize - 16, 8, 8);

        drawTypeIcon(g2, px + tileSize / 2, py + tileSize / 2);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, px + tileSize / 2 - fm.stringWidth(label) / 2,
                      py + tileSize - 4);

        if (floatingMsg != null && msgTimer > 0) {
            float fade = Math.min(1f, msgTimer / 0.5f);
            int fAlpha = (int)(fade * 220);
            int floatY = (int)(py - 10 - (2.5f - msgTimer) * 12);
            g2.setColor(new Color(255, 255, 200, fAlpha));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm2 = g2.getFontMetrics();
            int mw = fm2.stringWidth(floatingMsg);
            g2.fillRoundRect(px + tileSize / 2 - mw / 2 - 6, floatY - 14,
                             mw + 12, 18, 6, 6);
            g2.setColor(new Color(0x212121));
            g2.drawString(floatingMsg, px + tileSize / 2 - mw / 2, floatY);
        }
    }

    private void drawTypeIcon(Graphics2D g2, int cx, int cy) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        String icon = switch (type) {
            case PUZZLE_PANEL      -> "⚙";
            case LOCKED_DOOR       -> "🔒";
            case BOOKSHELF         -> "📖";
            case CLOCK             -> "🕐";
            case MIRROR            -> "◈";
            case CHEST             -> "▣";
            case COMPUTER_TERMINAL -> "⌨";
        };
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(icon, cx - fm.stringWidth(icon) / 2,
                      cy + fm.getAscent() / 2 - 4);
    }

    public void reset() {
        activated   = false;
        floatingMsg = null;
        msgTimer    = 0f;
    }

    public String           getId()             { return id; }
    public String           getLabel()          { return label; }
    public String           getHint()           { return hint; }
    public InteractableType getType()           { return type; }
    public int              getGridX()          { return gridX; }
    public int              getGridY()          { return gridY; }
    public String           getRequiredItemId() { return requiredItemId; }
    public boolean          isActivated()       { return activated; }
}
