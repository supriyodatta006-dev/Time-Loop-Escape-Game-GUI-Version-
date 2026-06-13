package game.ui.screens;

import game.ui.BaseScreen;
import game.ui.GameWindow;
import javax.swing.Timer;
import java.awt.*;

public class LoadingScreen extends BaseScreen {

    private float progress = 0f;
    private float spinAngle = 0f;
    private String statusText = "INITIALISING TEMPORAL LINK...";
    private Timer loadingTimer;

    private final String[] STATUS_MESSAGES = {
        "INITIALISING TEMPORAL CORE...",
        "ESTABLISHING QUANTUM ALIGNMENT...",
        "CONSTRUCTING DIMENSIONAL PORTALS...",
        "CALIBRATING CHRONO-COMPASS...",
        "SYNCHRONISING CHRONOLOGICAL LOOPS...",
        "STABILISING REALITY ANCHOR...",
        "LINK STABLE. SYSTEM READY."
    };

    public LoadingScreen(GameWindow window) {
        super(window);
    }

    @Override
    protected void init() {
        setLayout(null);

        loadingTimer = new Timer(20, e -> {
            spinAngle += 0.05f;

            float speed;
            if (progress < 25f) speed = 0.8f;
            else if (progress < 65f) speed = 0.4f;
            else if (progress < 85f) speed = 0.6f;
            else speed = 0.25f;

            progress += speed;

            int index = (int)(progress / 100f * STATUS_MESSAGES.length);
            index = Math.max(0, Math.min(STATUS_MESSAGES.length - 1, index));
            statusText = STATUS_MESSAGES[index];

            if (progress >= 100f) {
                progress = 100f;
                loadingTimer.stop();

                engine.showLogin();
            }
            repaint();
        });
        loadingTimer.start();
    }

    @Override
    protected void onResize(int w, int h) {

    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        int cx = w / 2;
        int cy = h / 2;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 188, 212, 25));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - 110, cy - 110, 220, 220);

        g2.setColor(new Color(0, 188, 212, 90));
        float[] dash = {8f, 16f};
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f, dash, spinAngle * 15f));
        g2.drawOval(cx - 95, cy - 95, 190, 190);

        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(ACCENT_CYAN);
        g2.drawLine(cx, cy, cx + (int)(Math.cos(spinAngle) * 75), cy + (int)(Math.sin(spinAngle) * 75));
        g2.setColor(ACCENT_PURPLE);
        g2.drawLine(cx, cy, cx + (int)(Math.cos(-spinAngle * 0.4f) * 55), cy + (int)(Math.sin(-spinAngle * 0.4f) * 55));

        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 6, cy - 6, 12, 12);
        g2.setColor(new Color(0, 188, 212, 140));
        g2.drawOval(cx - 10, cy - 10, 20, 20);

        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(TEXT_PRIMARY);
        String pct = String.format("%d%%", (int)progress);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pct, cx - fm.stringWidth(pct) / 2, cy + 150);

        int barW = 360;
        int barH = 6;
        int barX = cx - barW / 2;
        int barY = cy + 172;

        g2.setColor(new Color(0x0D, 0x15, 0x20));
        g2.fillRoundRect(barX, barY, barW, barH, 4, 4);
        g2.setColor(new Color(0x1C, 0x2A, 0x3A));
        g2.drawRoundRect(barX, barY, barW, barH, 4, 4);

        int fillW = (int)(barW * (progress / 100f));
        if (fillW > 0) {
            GradientPaint gp = new GradientPaint(barX, barY, ACCENT_CYAN, barX + fillW, barY, ACCENT_PURPLE);
            g2.setPaint(gp);
            g2.fillRoundRect(barX, barY, fillW, barH, 4, 4);
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(TEXT_DIM);
        fm = g2.getFontMetrics();
        g2.drawString(statusText, cx - fm.stringWidth(statusText) / 2, cy + 208);

        g2.setColor(new Color(0, 188, 212, 35));
        g2.drawLine(cx - 200, cy + 230, cx + 200, cy + 230);
        g2.fillRect(cx - 5, cy + 228, 10, 5);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (loadingTimer != null) {
            loadingTimer.stop();
            loadingTimer = null;
        }
    }
}
