package game.ui.screens;

import game.ui.BaseScreen;
import game.ui.GameWindow;
import game.utils.Profile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

public class MainMenuScreen extends BaseScreen {

    private float clockAngle = 0f;
    private float titleGlow  = 0f;
    private boolean glowUp   = true;
    private float logoScale  = 0.3f;

    public MainMenuScreen(GameWindow window) { super(window); }

    @Override protected void init() {}

    @Override
    protected void onResize(int w, int h) {
        removeAll();

        boolean isPremium = engine.getPremiumManager().isPremium();

        int bw = Math.min(260, w - 60);
        int bh = 50;
        int cx = w / 2 - bw / 2;
        int startY = (int)(h * 0.45f);
        int gap    = 48;

        JButton playBtn     = createButton("▶   PLAY",     ACCENT_CYAN,   () -> engine.showDifficultySelect());
        JButton tutorialBtn = createButton("📖  HOW TO PLAY", ACCENT_CYAN,   () -> engine.showTutorial());
        JButton profileBtn  = createButton("👤  PROFILE",  ACCENT_PURPLE, () -> engine.showProfile());
        JButton settingsBtn = createButton("⚙   SETTINGS", ACCENT_AMBER,  () -> engine.showSettings());

        String premLabel = isPremium ? "✓   PREMIUM"   : "💎   PREMIUM";
        Color  premColor = isPremium ? new Color(0x69, 0xF0, 0xAE) : new Color(0xFF, 0xB3, 0x00);
        JButton premBtn  = createButton(premLabel, premColor, () -> engine.showPremium());

        JButton quitBtn = createButton("✕   QUIT", new Color(0xEF, 0x53, 0x50), () -> engine.stop());

        playBtn.setBounds(cx, startY,               bw, bh);
        tutorialBtn.setBounds(cx, startY + gap,     bw, bh);
        profileBtn.setBounds(cx, startY + gap * 2,  bw, bh);
        settingsBtn.setBounds(cx, startY + gap * 3, bw, bh);
        premBtn.setBounds(cx, startY + gap * 4,     bw, bh);
        quitBtn.setBounds(cx, startY + gap * 5,     bw, bh);

        add(playBtn); add(tutorialBtn); add(profileBtn); add(settingsBtn); add(premBtn); add(quitBtn);

        JButton logoutBtn = makeLogoutButton();
        int lbw = 134;
        logoutBtn.setBounds(w - lbw - 14, 16, lbw, 36);
        add(logoutBtn);
    }

    private JButton makeLogoutButton() {
        JButton btn = new JButton("⏏  LOGOUT") {
            private boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent  e) { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(hovered ? new Color(0xEF,0x53,0x50,30) : new Color(0x0D,0x15,0x20));
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(hovered ? new Color(0xEF,0x53,0x50,180) : new Color(0x37,0x47,0x4F));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w-2, h-2, 8, 8);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.setColor(hovered ? new Color(0xFF,0x52,0x52) : new Color(0x78,0x90,0x9C));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), w/2-fm.stringWidth(getText())/2,
                        h/2+fm.getAscent()/2-fm.getDescent()/2);
                g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                    "Log out and return to the login screen?",
                    "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (c == JOptionPane.YES_OPTION) engine.logout();
        });
        return btn;
    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        if (logoScale < 1f) logoScale = Math.min(1f, logoScale + 0.03f);
        titleGlow += glowUp ? 0.02f : -0.02f;
        if (titleGlow > 1f) { titleGlow = 1f; glowUp = false; }
        if (titleGlow < 0f) { titleGlow = 0f; glowUp = true;  }
        clockAngle += 0.003f;

        drawBackgroundClock(g2, w, h);
        drawTitle(g2, w, h);
        drawUserBadge(g2, w, h);
        drawFooter(g2, w, h);
    }

    private void drawBackgroundClock(Graphics2D g2, int w, int h) {
        int cx = w/2, cy = h/2 - 20;
        int radius = Math.min(w, h)/2 + 30;
        g2.setColor(new Color(0, 188, 212, 14));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(cx-radius, cy-radius, radius*2, radius*2);
        for (int i = 0; i < 12; i++) {
            double a = Math.PI*2*i/12 + clockAngle*0.08;
            int mx = (int)(cx + Math.cos(a)*(radius-14));
            int my = (int)(cy + Math.sin(a)*(radius-14));
            g2.setColor(new Color(0, 188, 212, i%3==0 ? 50 : 25));
            g2.fillOval(mx-3, my-3, 6, 6);
        }
        drawHand(g2, cx, cy, clockAngle*0.4f,  radius-55, new Color(0,188,212,30), 2);
        drawHand(g2, cx, cy, clockAngle,        radius-75, new Color(0,188,212,45), 1);
        drawHand(g2, cx, cy, clockAngle*5f,     radius-85, new Color(0xEF,0x53,0x50,40), 1);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawHand(Graphics2D g2, int cx, int cy, float angle, int len, Color c, int width) {
        g2.setColor(c);
        g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy, cx+(int)(Math.cos(angle-Math.PI/2)*len), cy+(int)(Math.sin(angle-Math.PI/2)*len));
    }

    private void drawTitle(Graphics2D g2, int w, int h) {
        AffineTransform old = g2.getTransform();
        g2.translate(w/2.0, h*0.25);
        g2.scale(logoScale, logoScale);

        String line1 = "TIME LOOP", line2 = "ESCAPE";

        for (int glow = 10; glow >= 1; glow--) {
            float a = (0.03f + titleGlow*0.025f)*((10-glow)/10f);
            g2.setColor(new Color(0,188,212,(int)(a*255)));
            g2.setFont(new Font("Monospaced", Font.BOLD, 54+glow*2));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(line1, -fm.stringWidth(line1)/2, -20);
        }
        g2.setFont(new Font("Monospaced", Font.BOLD, 54));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(TEXT_PRIMARY);
        g2.drawString(line1, -fm.stringWidth(line1)/2, -20);

        for (int glow = 10; glow >= 1; glow--) {
            float a = (0.04f + titleGlow*0.03f)*((10-glow)/10f);
            g2.setColor(new Color(0,188,212,(int)(a*255)));
            g2.setFont(new Font("Monospaced", Font.BOLD, 70+glow*2));
            fm = g2.getFontMetrics();
            g2.drawString(line2, -fm.stringWidth(line2)/2, 58);
        }
        g2.setFont(new Font("Monospaced", Font.BOLD, 70));
        fm = g2.getFontMetrics();
        g2.setColor(ACCENT_CYAN);
        g2.drawString(line2, -fm.stringWidth(line2)/2, 58);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        fm = g2.getFontMetrics();
        g2.setColor(new Color(0x78,0x90,0x9C,160));
        String sub = "— ESCAPE THE LOOP OR RELIVE IT FOREVER —";
        g2.drawString(sub, -fm.stringWidth(sub)/2, 90);
        g2.setTransform(old);
    }

    private void drawUserBadge(Graphics2D g2, int w, int h) {
        Profile prof = engine.getProfileManager().getActiveProfile();
        if (prof == null) return;
        boolean isGuest = prof.isGuest();
        String  uLabel  = isGuest ? "👤 GUEST" : "👤 " + prof.getName().toUpperCase();
        Color   badgeC  = isGuest ? new Color(0x54,0x6E,0x7A) : ACCENT_CYAN;
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(uLabel)+20, bx = 16, by = 16;
        g2.setColor(new Color(badgeC.getRed(),badgeC.getGreen(),badgeC.getBlue(),20));
        g2.fillRoundRect(bx, by, bw, 28, 8, 8);
        g2.setColor(new Color(badgeC.getRed(),badgeC.getGreen(),badgeC.getBlue(),100));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(bx, by, bw, 28, 8, 8);
        g2.setColor(badgeC);
        g2.drawString(uLabel, bx+10, by+18);
        if (isGuest) {
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.setColor(new Color(0x54,0x6E,0x7A));
            g2.drawString("Progress not saved", bx, by+40);
        }
    }

    private void drawFooter(Graphics2D g2, int w, int h) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(0x37,0x47,0x4F));
        g2.drawString("v1.0  |  University Project", 14, h-14);
    }
}
