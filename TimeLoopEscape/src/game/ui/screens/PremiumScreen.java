package game.ui.screens;

import game.ui.BaseScreen;
import game.ui.GameWindow;
import game.utils.PremiumManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PremiumScreen extends BaseScreen {
    private PremiumManager premium;
    private float glowPhase = 0f;
    private float starPhase = 0f;

    private JTextField keyField;
    private JLabel     statusLabel;

    private static final String[][] FEATURES = {
        { "⚡",  "Difficulty",         "Easy & Normal",   "All 4 modes" },
        { "👤",  "Profile Slots",      "1 slot",          "3 slots" },
        { "💡",  "Hint System",        "No hints",        "3 hints/run" },
        { "🎨",  "Themes",             "Default only",    "3 extra themes" },
        { "🏆",  "Leaderboard",        "Local only",      "Global board" },
    };

    public PremiumScreen(GameWindow window) {
        super(window);
    }

    @Override
    protected void init() {
        premium = PremiumManager.getInstance();
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        top.setOpaque(false);
        top.add(createButton("← BACK", TEXT_DIM, () -> engine.showMainMenu()));
        add(top, BorderLayout.NORTH);

        JPanel bottom = buildBottomPanel();
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 40, 28, 40));

        if (premium.isPremium()) {

            JLabel lbl = new JLabel("✓  PREMIUM UNLOCKED  —  Enjoy all features!", SwingConstants.CENTER);
            lbl.setFont(new Font("Monospaced", Font.BOLD, 15));
            lbl.setForeground(new Color(0x69, 0xF0, 0xAE));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lbl);
            panel.add(Box.createVerticalStrut(8));

            JButton back = createButton("← BACK TO MENU", ACCENT_CYAN,
                    () -> engine.showMainMenu());
            back.setAlignmentX(Component.CENTER_ALIGNMENT);
            back.setMaximumSize(new Dimension(320, 48));
            panel.add(back);
        } else {

            JButton buyBtn = new JButton("💎   BUY PREMIUM  —  $4.99") {
                private boolean hovered = false;
                private float pulse = 0f;
                { addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                }); }
                @Override protected void paintComponent(Graphics g) {
                    pulse = (pulse + 0.06f) % ((float)Math.PI * 2);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();

                    Color c1 = new Color(0xFF, 0xB3, 0x00);
                    Color c2 = new Color(0xFF, 0x6F, 0x00);
                    if (hovered) {
                        c1 = c1.brighter();
                        c2 = c2.brighter();
                    }
                    GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 12, 12);

                    float a = 0.15f + 0.12f * (float) Math.sin(pulse);
                    g2.setColor(new Color(255, 255, 255, (int)(a * 255)));
                    g2.fillRoundRect(0, 0, w, h/2, 12, 6);

                    g2.setFont(new Font("Monospaced", Font.BOLD, 16));
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), w/2 - fm.stringWidth(getText())/2,
                            h/2 + fm.getAscent()/2 - fm.getDescent()/2);
                    g2.dispose();

                    SwingUtilities.invokeLater(this::repaint);
                }
            };
            buyBtn.setOpaque(false);
            buyBtn.setContentAreaFilled(false);
            buyBtn.setBorderPainted(false);
            buyBtn.setFocusPainted(false);
            buyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            buyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            buyBtn.setMaximumSize(new Dimension(380, 54));
            buyBtn.setPreferredSize(new Dimension(380, 54));
            buyBtn.addActionListener(e -> doBuyNow());
            panel.add(buyBtn);
            panel.add(Box.createVerticalStrut(14));

            JLabel or = new JLabel("— or enter a licence key —", SwingConstants.CENTER);
            or.setFont(new Font("Monospaced", Font.PLAIN, 11));
            or.setForeground(TEXT_DIM);
            or.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(or);
            panel.add(Box.createVerticalStrut(8));

            JPanel keyRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            keyRow.setOpaque(false);
            keyField = new JTextField(22);
            styleKeyField(keyField, "Enter licence key...");
            keyRow.add(keyField);

            JButton activateBtn = createButton("ACTIVATE", ACCENT_AMBER, this::doActivateKey);
            activateBtn.setPreferredSize(new Dimension(110, 38));
            keyRow.add(activateBtn);
            panel.add(keyRow);
            panel.add(Box.createVerticalStrut(8));

            statusLabel = new JLabel(" ", SwingConstants.CENTER);
            statusLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
            statusLabel.setForeground(new Color(0xFF, 0x52, 0x52));
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(statusLabel);
        }

        return panel;
    }

    private void doBuyNow() {
        int choice = JOptionPane.showOptionDialog(
                this,
                "<html><b>Time Loop Escape — Premium</b><br><br>" +
                "Unlock all difficulty modes,<br>" +
                "3 profile slots, hints, and more!<br><br>" +
                "<b>Price: $4.99  (one-time purchase)</b><br><br>" +
                "This is a simulated payment for demo purposes.</html>",
                "Buy Premium",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{ "💎  Confirm Purchase", "Cancel" },
                "💎  Confirm Purchase"
        );

        if (choice == 0) {
            premium.simulatePurchase();
            showStatus("✓ Purchase successful! Restarting...", false);

            new Timer(900, ev -> {
                engine.showPremium();
            }) {{ setRepeats(false); start(); }};
        }
    }

    private void doActivateKey() {
        if (keyField == null) return;
        String key = keyField.getText().trim();
        if (key.isBlank() || key.equals("Enter licence key...")) {
            showStatus("Please enter a licence key.", true);
            return;
        }
        if (premium.activateWithKey(key)) {
            showStatus("✓ Key accepted! Restarting...", false);
            new Timer(900, ev -> engine.showPremium()) {{ setRepeats(false); start(); }};
        } else {
            showStatus("✗ Invalid key. Try: TIMELOOP-PREMIUM", true);
        }
    }

    private void showStatus(String msg, boolean error) {
        if (statusLabel == null) return;
        statusLabel.setText(msg.isBlank() ? " " : msg);
        statusLabel.setForeground(error
                ? new Color(0xFF, 0x52, 0x52)
                : new Color(0x69, 0xF0, 0xAE));
    }

    @Override
    protected void onResize(int w, int h) {}

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        glowPhase += 0.04f;
        starPhase += 0.015f;

        drawSparkles(g2, w, h);
        drawTitle(g2, w, h);
        drawFeatureTable(g2, w, h);
    }

    private void drawSparkles(Graphics2D g2, int w, int h) {
        for (int i = 0; i < 18; i++) {
            float seed = i * 137.5f;
            float x = (seed * 47.1f) % w;
            float y = (seed * 29.3f + starPhase * 14f * (1 + i % 3)) % h;
            float a = 0.1f + 0.2f * (float) Math.abs(Math.sin(starPhase * 1.2f + i));
            Color c = (i % 2 == 0) ? ACCENT_AMBER : new Color(0xFF, 0xD7, 0x00);
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(a * 255)));
            int sz = (i % 4 == 0) ? 4 : 2;
            g2.fillOval((int)x, (int)y, sz, sz);
        }
    }

    private void drawTitle(Graphics2D g2, int w, int h) {

        for (int ring = 6; ring >= 1; ring--) {
            float a = 0.03f + 0.02f * (float) Math.sin(glowPhase + ring * 0.4f);
            g2.setColor(new Color(255, 179, 0, (int)(a * 255)));
            int rs = ring * 22;
            g2.fillOval(w/2 - rs, 42 - rs/3, rs*2, rs/1);
        }

        if (premium.isPremium()) {

            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            String badge = "✓ PREMIUM ACTIVE";
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(badge) + 24;
            g2.setColor(new Color(0x69, 0xF0, 0xAE, 30));
            g2.fillRoundRect(w/2 - bw/2, 14, bw, 22, 6, 6);
            g2.setColor(new Color(0x69, 0xF0, 0xAE));
            g2.drawString(badge, w/2 - fm.stringWidth(badge)/2, 29);
        }

        g2.setFont(new Font("Monospaced", Font.BOLD, 38));
        String line1 = "UPGRADE TO";
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(255, 179, 0, 50));
        g2.drawString(line1, w/2 - fm.stringWidth(line1)/2 + 2, 62);
        g2.setColor(new Color(0xFF, 0xB3, 0x00));
        g2.drawString(line1, w/2 - fm.stringWidth(line1)/2, 60);

        g2.setFont(new Font("Monospaced", Font.BOLD, 52));
        String line2 = "💎  PREMIUM";
        fm = g2.getFontMetrics();
        Color gold = new Color(0xFF, 0xD7, 0x00);

        g2.setColor(new Color(gold.getRed(), gold.getGreen(), gold.getBlue(), 40));
        g2.drawString(line2, w/2 - fm.stringWidth(line2)/2, 112);
        g2.setColor(gold);
        g2.drawString(line2, w/2 - fm.stringWidth(line2)/2, 110);

        drawDivider(g2, 60, 126, w - 120, ACCENT_AMBER);
    }

    private void drawFeatureTable(Graphics2D g2, int w, int h) {

        int tableW = Math.min(820, w - 80);
        int tableX = w/2 - tableW/2;
        int tableY = 142;
        int rowH   = 44;
        int col1W  = 40;
        int col2W  = 160;
        int col3W  = (tableW - col1W - col2W) / 2;
        int col4W  = col3W;

        int hx = tableX;
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));

        int freeColX = hx + col1W + col2W;
        g2.setColor(new Color(0x1C, 0x2A, 0x3A));
        g2.fillRoundRect(freeColX + 4, tableY, col3W - 8, 28, 6, 6);
        g2.setColor(TEXT_DIM);
        FontMetrics fm = g2.getFontMetrics();
        String freeLabel = "🔓  FREE";
        g2.drawString(freeLabel, freeColX + col3W/2 - fm.stringWidth(freeLabel)/2, tableY + 18);

        int premColX = freeColX + col3W;
        Color goldBg = new Color(0xFF, 0xB3, 0x00, 28);
        g2.setColor(goldBg);
        g2.fillRoundRect(premColX + 4, tableY, col4W - 8, 28, 6, 6);
        g2.setColor(new Color(0xFF, 0xB3, 0x00, 150));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(premColX + 4, tableY, col4W - 8, 28, 6, 6);
        g2.setColor(ACCENT_AMBER);
        String premLabel = "💎  PREMIUM";
        g2.drawString(premLabel, premColX + col4W/2 - fm.stringWidth(premLabel)/2, tableY + 18);

        for (int i = 0; i < FEATURES.length; i++) {
            String[] row = FEATURES[i];
            int ry = tableY + 36 + i * rowH;

            if (i % 2 == 0) {
                g2.setColor(new Color(255, 255, 255, 4));
                g2.fillRect(tableX, ry, tableW, rowH - 2);
            }

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString(row[0], tableX + col1W/2 - 8, ry + 26);

            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(row[1], tableX + col1W + 8, ry + 26);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2.setColor(premium.isPremium()
                    ? new Color(0x54, 0x6E, 0x7A)
                    : TEXT_DIM);
            fm = g2.getFontMetrics();
            int fval_x = freeColX + col3W/2 - fm.stringWidth(row[2])/2;
            g2.drawString(row[2], fval_x, ry + 26);

            Color premC = premium.isPremium()
                    ? new Color(0x69, 0xF0, 0xAE)
                    : ACCENT_AMBER;
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(premC);
            fm = g2.getFontMetrics();
            String premVal = premium.isPremium() ? "✓  " + row[3] : row[3];
            int pval_x = premColX + col4W/2 - fm.stringWidth(premVal)/2;
            g2.drawString(premVal, pval_x, ry + 26);

            g2.setColor(new Color(0x1C, 0x2A, 0x3A));
            g2.fillRect(tableX, ry + rowH - 2, tableW, 1);
        }

        g2.setColor(new Color(0x1C, 0x2A, 0x3A));
        int divY = tableY + 28;
        int divH = FEATURES.length * rowH + 8;
        g2.fillRect(freeColX, divY, 1, divH);
        g2.fillRect(premColX, divY, 1, divH);
        g2.fillRect(tableX + tableW, divY, 1, divH);

        if (!premium.isPremium()) {
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.setColor(new Color(0x37, 0x47, 0x4F));
            String note = "Demo key:  TIMELOOP-PREMIUM  |  TIMELOOP-DEMO-2024  |  TEMPORAL-ESCAPE-PRO";
            fm = g2.getFontMetrics();
            g2.drawString(note, w/2 - fm.stringWidth(note)/2,
                    tableY + 36 + FEATURES.length * rowH + 22);
        }
    }

    private void styleKeyField(JTextField field, String placeholder) {
        field.setFont(new Font("Monospaced", Font.PLAIN, 13));
        field.setForeground(TEXT_DIM);
        field.setCaretColor(ACCENT_AMBER);
        field.setBackground(new Color(0x08, 0x10, 0x20));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x47, 0x4F), 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_DIM);
                }
            }
        });
        field.addActionListener(e -> doActivateKey());
    }
}
