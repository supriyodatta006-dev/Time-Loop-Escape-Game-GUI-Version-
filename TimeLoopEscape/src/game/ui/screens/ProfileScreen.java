package game.ui.screens;

import game.core.Difficulty;
import game.ui.BaseScreen;
import game.ui.GameWindow;
import game.utils.Profile;
import game.utils.ProfileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ProfileScreen extends BaseScreen {

    private final ProfileManager pm;

    public ProfileScreen(GameWindow window) {
        super(window);
        pm = engine.getProfileManager();
    }

    @Override protected void init() {}

    @Override
    protected void onResize(int w, int h) {
        removeAll();

        JButton backBtn = createButton("← BACK", TEXT_DIM, () -> engine.showMainMenu());
        backBtn.setBounds(20, 20, 160, 44);
        add(backBtn);

        List<Profile> profiles = pm.getProfiles();
        if (profiles.size() < 3) {
            JButton createBtn = createButton("+ NEW PROFILE", ACCENT_CYAN, () -> {
                String name = JOptionPane.showInputDialog(this,
                        "Enter profile name:", "New Profile", JOptionPane.PLAIN_MESSAGE);
                if (name != null && !name.isBlank()) {
                    try {
                        Profile p = pm.createProfile(name.trim());
                        pm.setActiveProfile(p);
                        onResize(getWidth(), getHeight());
                        revalidate(); repaint();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                    }
                }
            });
            int bw = 260;
            createBtn.setBounds(w/2 - bw/2, h-90, bw, 48);
            add(createBtn);
        }
        revalidate(); repaint();
    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 38));
        g2.setColor(ACCENT_PURPLE);
        String title = "PROFILES";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, w/2-fm.stringWidth(title)/2, 78);
        drawDivider(g2, 60, 96, w-120, ACCENT_PURPLE);

        List<Profile> profiles = pm.getProfiles();
        int slots   = 3;
        int cardW   = Math.min(280, (w-80)/slots - 20);
        int cardH   = 390;
        int spacing = 20;
        int totalW  = slots*cardW + (slots-1)*spacing;
        int startX  = w/2 - totalW/2;
        int cardY   = 118;

        for (int i = 0; i < slots; i++) {
            int cx = startX + i*(cardW+spacing);
            if (i < profiles.size())
                drawProfileCard(g2, profiles.get(i), cx, cardY, cardW, cardH,
                        profiles.get(i) == pm.getActiveProfile());
            else
                drawEmptySlot(g2, cx, cardY, cardW, cardH);
        }

        g2.setFont(FONT_SMALL);
        g2.setColor(TEXT_DIM);
        String hint = "Left-click to activate  •  Right-click to delete";
        g2.drawString(hint, w/2-g2.getFontMetrics().stringWidth(hint)/2, h-20);
    }

    private void drawProfileCard(Graphics2D g2, Profile p, int x, int y, int w, int h, boolean active) {
        Color ac = active ? ACCENT_CYAN : ACCENT_PURPLE;
        g2.setColor(new Color(0x0F,0x1C,0x2E));
        g2.fillRoundRect(x, y, w, h, 14, 14);
        if (active) {
            g2.setColor(new Color(ACCENT_CYAN.getRed(),ACCENT_CYAN.getGreen(),ACCENT_CYAN.getBlue(),18));
            g2.fillRoundRect(x, y, w, h, 14, 14);
        }
        g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),180));
        g2.fillRoundRect(x, y, w, 5, 4, 4);
        g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(), active?180:70));
        g2.setStroke(new BasicStroke(active?2f:1.2f));
        g2.drawRoundRect(x, y, w, h, 14, 14);
        g2.setStroke(new BasicStroke(1f));

        int acx = x+w/2, acy = y+52;
        g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),30));
        g2.fillOval(acx-32, acy-32, 64, 64);
        g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(acx-32, acy-32, 64, 64);
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 28));
        g2.setColor(ac);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("👤", acx-fm.stringWidth("👤")/2, acy+10);

        g2.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2.setColor(TEXT_PRIMARY);
        fm = g2.getFontMetrics();
        g2.drawString(p.getName(), x+w/2-fm.stringWidth(p.getName())/2, y+104);

        if (active) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.setColor(new Color(ACCENT_CYAN.getRed(),ACCENT_CYAN.getGreen(),ACCENT_CYAN.getBlue(),200));
            String badge = "● ACTIVE";
            fm = g2.getFontMetrics();
            g2.drawString(badge, x+w/2-fm.stringWidth(badge)/2, y+120);
        }

        g2.setColor(new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),40));
        g2.fillRect(x+16, y+128, w-32, 1);

        int sy = y+148, rowH = 28;
        drawStatRow(g2, x+14, sy,          w-28, "Games",      String.valueOf(p.getTotalGamesPlayed()), ac);
        drawStatRow(g2, x+14, sy+rowH,     w-28, "Escapes",    String.valueOf(p.getTotalEscapes()), ac);
        drawStatRow(g2, x+14, sy+rowH*2,   w-28, "Best Score", String.valueOf(p.getBestScore()), ac);
        drawStatRow(g2, x+14, sy+rowH*3,   w-28, "Playtime",   p.getFormattedPlaytime(), ac);
        drawStatRow(g2, x+14, sy+rowH*4,   w-28, "Escape Rate",String.format("%.0f%%",p.getEscapeRate()), ac);

        int bx = x+10, by = sy+rowH*5+8;
        int badgeW = (w-20-9)/4;
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        for (Difficulty d : Difficulty.values()) {
            boolean done = p.hasCompleted(d);
            g2.setColor(done ? new Color(d.accentColor.getRed(),d.accentColor.getGreen(),
                    d.accentColor.getBlue(),200) : new Color(0x1C,0x2A,0x3A));
            g2.fillRoundRect(bx, by, badgeW, 18, 4, 4);
            g2.setColor(done ? Color.WHITE : new Color(0x37,0x47,0x4F));
            fm = g2.getFontMetrics();
            String lbl = d.label.length()>5 ? d.label.substring(0,4) : d.label;
            g2.drawString(lbl, bx+badgeW/2-fm.stringWidth(lbl)/2, by+12);
            bx += badgeW+3;
        }
        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g2.setColor(new Color(0x37,0x47,0x4F));
        g2.drawString("Since "+p.getCreatedAt(), x+10, y+h-10);
    }

    private void drawEmptySlot(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(0x08,0x0D,0x18));
        g2.fillRoundRect(x, y, w, h, 14, 14);
        g2.setColor(new Color(0x1C,0x2A,0x3A));
        g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,
                0,new float[]{8f,6f},0f));
        g2.drawRoundRect(x+1,y+1,w-2,h-2,14,14);
        g2.setStroke(new BasicStroke(1f));
        int cx=x+w/2, cy=y+h/2-16;
        g2.setColor(new Color(0x26,0x32,0x38));
        g2.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g2.drawLine(cx-14,cy,cx+14,cy); g2.drawLine(cx,cy-14,cx,cy+14);
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(FONT_SMALL);
        g2.setColor(new Color(0x37,0x47,0x4F));
        String lbl="Empty Slot";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(lbl,cx-fm.stringWidth(lbl)/2,cy+36);
        g2.setFont(new Font("Monospaced",Font.PLAIN,10));
        g2.setColor(new Color(0x26,0x32,0x38));
        String sub="Click + NEW PROFILE";
        fm=g2.getFontMetrics();
        g2.drawString(sub,cx-fm.stringWidth(sub)/2,cy+52);
    }

    private void drawStatRow(Graphics2D g2, int x, int y, int w,
                              String label, String value, Color ac) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(TEXT_DIM); g2.drawString(label, x, y);
        g2.setColor(ac);
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(value, x+w-fm.stringWidth(value), y);
        g2.setColor(new Color(0x1C,0x2A,0x3A));
        g2.fillRect(x, y+3, w, 1);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        List<Profile> profiles = pm.getProfiles();
        int w = getWidth(), slots = 3;
        int cardW   = Math.min(280, (w-80)/slots - 20);
        int cardH   = 390, spacing = 20;
        int totalW  = slots*cardW + (slots-1)*spacing;
        int startX  = w/2 - totalW/2;
        int cardY   = 118;

        for (int i = 0; i < profiles.size(); i++) {
            int cx = startX + i*(cardW+spacing);
            if (e.getX()>=cx && e.getX()<=cx+cardW && e.getY()>=cardY && e.getY()<=cardY+cardH) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pm.setActiveProfile(profiles.get(i));
                    pm.saveProfiles(); repaint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    int opt = JOptionPane.showConfirmDialog(this,
                            "Delete profile '" + profiles.get(i).getName() + "'?",
                            "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (opt == JOptionPane.YES_OPTION) {
                        pm.deleteProfile(profiles.get(i));
                        pm.saveProfiles();
                        onResize(getWidth(), getHeight());
                        revalidate(); repaint();
                    }
                }
                return;
            }
        }
    }
}
