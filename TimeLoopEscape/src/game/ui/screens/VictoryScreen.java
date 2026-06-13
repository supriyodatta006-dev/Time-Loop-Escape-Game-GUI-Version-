package game.ui.screens;

import game.core.GameSession;
import game.core.Difficulty;
import game.ui.BaseScreen;
import game.ui.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class VictoryScreen extends BaseScreen {

    private final GameSession session;

    private float glowPhase  = 0f;
    private float starPhase  = 0f;
    private float slideIn    = 0f;

    private JButton playAgainBtn;
    private JButton menuBtn;

    public VictoryScreen(GameWindow window, GameSession session) {
        super(window);
        this.session = session;

        engine.getProfileManager().getActiveProfile().recordGame(
                session.getScore(), true,
                session.getDifficulty(),
                session.getTotalElapsedMs()
        );
        engine.getProfileManager().saveProfiles();
    }

    @Override
    protected void init() {
        setLayout(null);

        playAgainBtn = createButton("▶  PLAY AGAIN", ACCENT_CYAN, () ->
                engine.showDifficultySelect());
        add(playAgainBtn);

        menuBtn = createButton("⌂  MAIN MENU", ACCENT_PURPLE, () ->
                engine.showMainMenu());
        add(menuBtn);
    }

    @Override
    protected void onResize(int w, int h) {
        int cx = w / 2;
        int panelH = 240;
        int py = (int)(h * 0.42f);
        int bottom = py + panelH;

        if (playAgainBtn != null) {
            playAgainBtn.setBounds(cx - 150, bottom + 16, 300, 52);
        }
        if (menuBtn != null) {
            menuBtn.setBounds(cx - 150, bottom + 76, 300, 52);
        }
    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        glowPhase += 0.04f;
        starPhase += 0.02f;
        if (slideIn < 1f) slideIn = Math.min(1f, slideIn + 0.025f);

        drawCelebrationStars(g2, w, h);
        drawTitle(g2, w, h);
        drawStatsPanel(g2, w, h);
    }

    private void drawCelebrationStars(Graphics2D g2, int w, int h) {
        for (int i = 0; i < 24; i++) {
            float seed  = i * 97.3f;
            float x     = ((seed * 53.1f) % w);
            float y     = ((seed * 37.7f + starPhase * 18f * (1 + i % 3)) % h);
            float alpha = 0.15f + 0.25f * (float) Math.abs(Math.sin(starPhase * 1.5f + i));
            int   sz    = 2 + (i % 4 == 0 ? 3 : 1);
            Color c     = (i % 3 == 0) ? ACCENT_CYAN
                        : (i % 3 == 1) ? ACCENT_AMBER : new Color(0x00E676);
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255)));
            g2.fillOval((int) x, (int) y, sz, sz);
        }
    }

    private void drawTitle(Graphics2D g2, int w, int h) {

        float ease = 1f - (1f - slideIn) * (1f - slideIn);
        int   topY = (int)(h * 0.18f - (1f - ease) * 80);

        for (int ring = 8; ring >= 1; ring--) {
            float a = 0.025f + 0.02f * (float) Math.sin(glowPhase + ring * 0.4f);
            g2.setColor(new Color(0, 230, 118, (int)(a * 255)));
            int rs = ring * 28;
            g2.fillOval(w / 2 - rs, topY - rs / 2, rs * 2, rs);
        }

        String line1 = "YOU ESCAPED";
        String line2 = "THE LOOP!";

        g2.setFont(new Font("Monospaced", Font.BOLD, 52));
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawString(line1, w / 2 - fm.stringWidth(line1) / 2 + 2, topY + 2);

        g2.setColor(new Color(0x69F0AE));
        g2.drawString(line1, w / 2 - fm.stringWidth(line1) / 2, topY);

        g2.setFont(new Font("Monospaced", Font.BOLD, 68));
        fm = g2.getFontMetrics();
        float pulseScale = 1f + 0.012f * (float) Math.sin(glowPhase * 2);
        AffineTransform old = g2.getTransform();
        g2.translate(w / 2.0, topY + 72);
        g2.scale(pulseScale, pulseScale);
        g2.setColor(ACCENT_CYAN);
        g2.drawString(line2, -fm.stringWidth(line2) / 2, 0);
        g2.setTransform(old);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(TEXT_DIM);
        String tag = "The loop is broken. You are free.";
        g2.drawString(tag, w / 2 - g2.getFontMetrics().stringWidth(tag) / 2, topY + 130);
    }

    private void drawStatsPanel(Graphics2D g2, int w, int h) {
        int panelW = 480, panelH = 240;
        int px = w / 2 - panelW / 2;
        int py = (int)(h * 0.42f);

        g2.setColor(new Color(0x0D1828));
        g2.fillRoundRect(px, py, panelW, panelH, 16, 16);

        float borderAlpha = 0.5f + 0.3f * (float) Math.sin(glowPhase);
        g2.setColor(new Color(ACCENT_CYAN.getRed(), ACCENT_CYAN.getGreen(),
                              ACCENT_CYAN.getBlue(), (int)(borderAlpha * 255)));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(px, py, panelW, panelH, 16, 16);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(0, 230, 118, 60));
        g2.fillRoundRect(px, py, panelW, 6, 4, 4);

        int rowH = 44;
        int sy   = py + 28;

        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(TEXT_DIM);
        g2.drawString("FINAL SCORE", px + 20, sy);
        g2.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2.setColor(ACCENT_AMBER);
        String scoreStr = String.format("%,d", session.getScore());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(scoreStr, px + panelW - fm.stringWidth(scoreStr) - 20, sy + 2);

        drawDivider(g2, px + 16, sy + 12, panelW - 32, new Color(0x1C2A3A));

        long secs    = session.getTotalElapsedMs() / 1000;
        String time  = String.format("%d:%02d", secs / 60, secs % 60);
        Difficulty d = session.getDifficulty();

        drawStatRow(g2, px + 20, sy + rowH,       panelW - 40, "Loops used",
                session.getLoopCount() + " / " + GameSession.MAX_LOOPS,
                session.getLoopCount() <= 2 ? new Color(0x69F0AE) : ACCENT_AMBER);

        drawStatRow(g2, px + 20, sy + rowH * 2,   panelW - 40, "Time taken",
                time, ACCENT_CYAN);

        drawStatRow(g2, px + 20, sy + rowH * 3,   panelW - 40, "Difficulty",
                d.label, d.accentColor);

        drawStatRow(g2, px + 20, sy + rowH * 4,   panelW - 40, "Score multiplier",
                d.getScoreMultiplier() + "×", d.accentColor);

        drawStatRow(g2, px + 20, sy + rowH * 4 + 22, panelW - 40, "Clues found",
                session.getClueCount() + " clues", TEXT_DIM);
    }

    private void drawStatRow(Graphics2D g2, int x, int y, int w,
                              String label, String value, Color valueColor) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(TEXT_DIM);
        g2.drawString(label, x, y);
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(valueColor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(value, x + w - fm.stringWidth(value), y);
    }
}
