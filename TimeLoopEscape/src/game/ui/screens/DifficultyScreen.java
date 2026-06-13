package game.ui.screens;

import game.core.Difficulty;
import game.ui.BaseScreen;
import game.ui.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DifficultyScreen extends BaseScreen {

    private int     hoveredCard = -1;
    private final float[] cardGlow = new float[Difficulty.values().length];

    public DifficultyScreen(GameWindow window) { super(window); }

    @Override protected void init() {}

    @Override
    protected void onResize(int w, int h) {
        removeAll();
        JButton backBtn = createButton("← BACK", TEXT_DIM, () -> engine.showMainMenu());
        backBtn.setBounds(20, 20, 160, 44);
        add(backBtn);
    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 38));
        String title = "SELECT DIFFICULTY";
        FontMetrics fm = g2.getFontMetrics();
        int tx = w/2 - fm.stringWidth(title)/2;
        g2.setColor(new Color(ACCENT_CYAN.getRed(),ACCENT_CYAN.getGreen(),ACCENT_CYAN.getBlue(),50));
        g2.drawString(title, tx+1, 79);
        g2.setColor(ACCENT_CYAN);
        g2.drawString(title, tx, 78);
        drawDivider(g2, 60, 96, w-120, ACCENT_CYAN);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_DIM);
        String sub = "Click a card to start  —  hover to preview";
        g2.drawString(sub, w/2 - g2.getFontMetrics().stringWidth(sub)/2, 114);

        Difficulty[] diffs = Difficulty.values();
        int cardW   = Math.min(210, (w-60)/diffs.length - 16);
        int cardH   = 340;
        int spacing = 16;
        int totalW  = diffs.length*cardW + (diffs.length-1)*spacing;
        int startX  = w/2 - totalW/2;
        int cardY   = (h-cardH)/2 + 30;

        for (int i = 0; i < diffs.length; i++) {
            cardGlow[i] = i == hoveredCard
                    ? Math.min(1f, cardGlow[i]+0.07f)
                    : Math.max(0f, cardGlow[i]-0.05f);
            boolean locked = !engine.getPremiumManager().isDifficultyAllowed(diffs[i]);
            drawDiffCard(g2, diffs[i], startX + i*(cardW+spacing), cardY, cardW, cardH, cardGlow[i], locked);
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(0x37,0x47,0x4F));
        String hint = "Higher difficulty = bigger score multiplier";
        g2.drawString(hint, w/2 - g2.getFontMetrics().stringWidth(hint)/2, h-18);
    }

    private void drawDiffCard(Graphics2D g2, Difficulty diff, int x, int y, int w, int h, float glow, boolean locked) {
        Color ac = diff.accentColor;
        g2.setColor(new Color(0,0,0,(int)(40+glow*60)));
        g2.fillRoundRect(x+4, y+6, w, h, 16, 16);
        g2.setColor(new Color(0x0D,0x18,0x28));
        g2.fillRoundRect(x, y, w, h, 16, 16);
        if (glow > 0.01f) {
            g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),(int)(glow*18)));
            g2.fillRoundRect(x, y, w, h, 16, 16);
            g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),(int)(glow*100)));
            g2.setStroke(new BasicStroke(4f));
            g2.drawRoundRect(x-2, y-2, w+4, h+4, 18, 18);
            g2.setStroke(new BasicStroke(1f));
        }
        g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(), 60+(int)(glow*130)));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 16, 16);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(ac);
        g2.fillRoundRect(x, y, w, 5, 4, 4);

        String icon = switch (diff) {
            case EASY -> "🌀"; case NORMAL -> "⏳"; case HARD -> "⚡"; case NIGHTMARE -> "💀";
        };
        g2.setFont(new Font("SansSerif", Font.PLAIN, 32));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(icon, x+w/2-fm.stringWidth(icon)/2, y+60);

        g2.setFont(new Font("Monospaced", Font.BOLD, 17));
        fm = g2.getFontMetrics();
        g2.setColor(ac);
        g2.drawString(diff.label, x+w/2-fm.stringWidth(diff.label)/2, y+90);

        g2.setFont(new Font("SansSerif", Font.ITALIC, 10));
        g2.setColor(TEXT_DIM);
        drawWrapped(g2, diff.tagline, x+12, y+108, w-24, 13);

        g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),40));
        g2.fillRect(x+12, y+140, w-24, 1);

        int sy = y+158;
        drawStat(g2, x+12, sy,      w-24, "⏱ Time",       diff.timeLimitSeconds+"s",          ac);
        drawStat(g2, x+12, sy+28,   w-24, "🔍 Clues",      diff.startingVisibleClues+"",        ac);
        drawStat(g2, x+12, sy+56,   w-24, "⚠ Penalty",    diff.penaltySeconds+"s",             ac);
        drawStat(g2, x+12, sy+84,   w-24, "✦ Multiplier",  diff.getScoreMultiplier()+"×",       ac);

        if (glow > 0.01f && !locked) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            String cta = "▶ CLICK TO START";
            fm = g2.getFontMetrics();
            g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),(int)(glow*220)));
            g2.fillRoundRect(x+14, y+h-38, w-28, 26, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(cta, x+w/2-fm.stringWidth(cta)/2, y+h-20);
        }

        if (locked) {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(x, y, w, h, 16, 16);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 36));
            fm = g2.getFontMetrics();
            g2.drawString("🔒", x + w/2 - fm.stringWidth("🔒")/2, y + h/2 - 10);

            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            fm = g2.getFontMetrics();
            String pLbl = "💎 PREMIUM ONLY";
            g2.setColor(new Color(0xFF, 0xB3, 0x00, 220));
            g2.fillRoundRect(x+10, y+h-38, w-20, 24, 6, 6);
            g2.setColor(new Color(0x0D, 0x15, 0x20));
            g2.drawString(pLbl, x + w/2 - fm.stringWidth(pLbl)/2, y + h - 22);
        }
    }

    private void drawStat(Graphics2D g2, int x, int y, int w, String label, String value, Color ac) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT_DIM);
        g2.drawString(label, x, y);
        g2.setColor(ac);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(value, x+w-fm.stringWidth(value), y);
        g2.setColor(new Color(0x1C,0x2A,0x3A));
        g2.fillRect(x, y+3, w, 1);
    }

    private void drawWrapped(Graphics2D g2, String text, int x, int y, int maxW, int lineH) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (String word : words) {
            String test = line.isEmpty() ? word : line+" "+word;
            if (fm.stringWidth(test) > maxW && !line.isEmpty()) {
                g2.drawString(line.toString(), x, curY);
                line = new StringBuilder(word);
                curY += lineH;
            } else line = new StringBuilder(test);
        }
        if (!line.isEmpty()) g2.drawString(line.toString(), x, curY);
    }

    @Override public void mouseMoved(MouseEvent e) { hoveredCard = cardAt(e.getX(), e.getY()); }
    @Override public void mousePressed(MouseEvent e) {
        int idx = cardAt(e.getX(), e.getY());
        if (idx >= 0) {
            Difficulty d = Difficulty.values()[idx];
            if (!engine.getPremiumManager().isDifficultyAllowed(d)) {

                engine.showPremium();
            } else {
                engine.startGame(d);
            }
        }
    }

    private int cardAt(int mx, int my) {
        int w = getWidth(), h = getHeight();
        Difficulty[] diffs = Difficulty.values();
        int cardW   = Math.min(210, (w-60)/diffs.length - 16);
        int cardH   = 340, spacing = 16;
        int totalW  = diffs.length*cardW + (diffs.length-1)*spacing;
        int startX  = w/2 - totalW/2;
        int cardY   = (h-cardH)/2 + 30;
        for (int i = 0; i < diffs.length; i++) {
            int cx = startX + i*(cardW+spacing);
            if (mx>=cx && mx<=cx+cardW && my>=cardY && my<=cardY+cardH) return i;
        }
        return -1;
    }
}
