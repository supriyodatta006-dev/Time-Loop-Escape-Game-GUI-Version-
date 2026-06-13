package game.ui.screens;

import game.core.GameSession;
import game.ui.BaseScreen;
import game.ui.GameWindow;

import javax.swing.*;
import java.awt.*;

public class GameOverScreen extends BaseScreen {

    private final GameSession session;

    private float glitchPhase = 0f;
    private float slideIn     = 0f;

    private JButton retryBtn;
    private JButton menuBtn;

    public GameOverScreen(GameWindow window, GameSession session) {
        super(window);
        this.session = session;

        engine.getProfileManager().getActiveProfile().recordGame(
                session.getScore(), false,
                session.getDifficulty(),
                session.getTotalElapsedMs()
        );
        engine.getProfileManager().saveProfiles();
    }

    @Override
    protected void init() {
        setLayout(null);

        retryBtn = createButton("↺  TRY AGAIN", new Color(0xEF5350), () ->
                engine.showDifficultySelect());
        add(retryBtn);

        menuBtn = createButton("⌂  MAIN MENU", TEXT_DIM, () ->
                engine.showMainMenu());
        add(menuBtn);
    }

    @Override
    protected void onResize(int w, int h) {
        int cx = w / 2;
        int panelH = 210;
        int py = (int)(h * 0.44f);
        int bottom = py + panelH;

        if (retryBtn != null) {
            retryBtn.setBounds(cx - 150, bottom + 24, 300, 52);
        }
        if (menuBtn != null) {
            menuBtn.setBounds(cx - 150, bottom + 84, 300, 52);
        }
    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        glitchPhase += 0.05f;
        if (slideIn < 1f) slideIn = Math.min(1f, slideIn + 0.02f);

        drawTitle(g2, w, h);
        drawStatsPanel(g2, w, h);
    }

    private void drawTitle(Graphics2D g2, int w, int h) {
        float ease = 1f - (1f - slideIn) * (1f - slideIn);
        int topY   = (int)(h * 0.20f - (1f - ease) * 80);

        for (int ring = 6; ring >= 1; ring--) {
            float a = 0.02f + 0.015f * (float) Math.sin(glitchPhase + ring * 0.5f);
            g2.setColor(new Color(220, 0, 0, (int)(a * 255)));
            int rs = ring * 30;
            g2.fillOval(w / 2 - rs, topY - rs / 2, rs * 2, rs);
        }

        String line1 = "THE LOOP";
        String line2 = "CONSUMED YOU";

        int gx = (int)(Math.sin(glitchPhase * 7.3f) * 3);

        g2.setFont(new Font("Monospaced", Font.BOLD, 52));
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(new Color(255, 0, 0, 60));
        g2.drawString(line1, w / 2 - fm.stringWidth(line1) / 2 + gx + 3, topY);
        g2.setColor(new Color(0, 255, 200, 40));
        g2.drawString(line1, w / 2 - fm.stringWidth(line1) / 2 + gx - 3, topY);

        g2.setColor(new Color(0xFF5252));
        g2.drawString(line1, w / 2 - fm.stringWidth(line1) / 2 + gx, topY);

        g2.setFont(new Font("Monospaced", Font.BOLD, 56));
        fm = g2.getFontMetrics();
        g2.setColor(new Color(255, 80, 80, 55));
        g2.drawString(line2, w / 2 - fm.stringWidth(line2) / 2 + gx + 2, topY + 68);
        g2.setColor(new Color(0xFF1744));
        g2.drawString(line2, w / 2 - fm.stringWidth(line2) / 2 + gx, topY + 68);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(new Color(0x546E7A));
        String tag = "You ran out of loops. The loop runs forever now.";
        g2.drawString(tag, w / 2 - g2.getFontMetrics().stringWidth(tag) / 2, topY + 108);
    }

    private void drawStatsPanel(Graphics2D g2, int w, int h) {
        int panelW = 460, panelH = 210;
        int px = w / 2 - panelW / 2;
        int py = (int)(h * 0.44f);

        g2.setColor(new Color(0x0D1828));
        g2.fillRoundRect(px, py, panelW, panelH, 16, 16);

        g2.setColor(new Color(0xFF, 0x52, 0x52, 100));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(px, py, panelW, panelH, 16, 16);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(255, 82, 82, 80));
        g2.fillRoundRect(px, py, panelW, 6, 4, 4);

        int rowH = 42;
        int sy   = py + 30;

        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(TEXT_DIM);
        g2.drawString("FINAL SCORE", px + 20, sy);
        g2.setFont(new Font("Monospaced", Font.BOLD, 26));
        g2.setColor(new Color(0xFF5252));
        String scoreStr = String.format("%,d", session.getScore());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(scoreStr, px + panelW - fm.stringWidth(scoreStr) - 20, sy + 2);

        drawDivider(g2, px + 16, sy + 12, panelW - 32, new Color(0x1C2A3A));

        long secs   = session.getTotalElapsedMs() / 1000;
        String time = String.format("%d:%02d", secs / 60, secs % 60);

        drawStatRow(g2, px + 20, sy + rowH,     panelW - 40, "Loops survived",
                GameSession.MAX_LOOPS + " / " + GameSession.MAX_LOOPS, new Color(0xFF5252));
        drawStatRow(g2, px + 20, sy + rowH * 2, panelW - 40, "Time elapsed",   time, TEXT_DIM);
        drawStatRow(g2, px + 20, sy + rowH * 3, panelW - 40, "Clues found",
                session.getClueCount() + " clues", ACCENT_AMBER);
        drawStatRow(g2, px + 20, sy + rowH * 4, panelW - 40, "Difficulty",
                session.getDifficulty().label, session.getDifficulty().accentColor);
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
