package game.ui.screens;

import game.ui.BaseScreen;
import game.ui.GameWindow;
import game.utils.SettingsManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

public class SettingsScreen extends BaseScreen {

    private SettingsManager settings;

    private JSlider masterSlider, musicSlider, sfxSlider;
    private JCheckBox musicToggle, sfxToggle, fpsToggle;

    public SettingsScreen(GameWindow window) {
        super(window);

    }

    @Override
    protected void init() {

        settings = engine.getSettingsManager();

        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        topBar.setOpaque(false);
        JButton backBtn = createButton("← BACK", TEXT_DIM, () -> engine.showMainMenu());
        topBar.add(backBtn);
        add(topBar, BorderLayout.NORTH);

        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        centre.add(Box.createVerticalStrut(8));

        JPanel card = buildCard();
        JPanel cardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cardWrapper.setOpaque(false);
        cardWrapper.add(card);
        centre.add(cardWrapper);

        centre.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);

        JButton saveBtn = createButton("✔  SAVE & CLOSE", ACCENT_CYAN, () -> {
            settings.save();
            engine.showMainMenu();
        });
        JButton resetBtn = createButton("↺  RESET DEFAULTS", new Color(0xEF, 0x53, 0x50), () -> {
            masterSlider.setValue(80);
            musicSlider.setValue(60);
            sfxSlider.setValue(90);
            musicToggle.setSelected(true);
            musicToggle.setText("ON");
            sfxToggle.setSelected(true);
            sfxToggle.setText("ON");
            fpsToggle.setSelected(false);
            fpsToggle.setText("OFF");
            audio.setMasterVolume(0.8f);
            audio.setMusicVolume(0.6f);
            audio.setSFXVolume(0.9f);
            audio.setMusicMuted(false);
            audio.setSFXMuted(false);
            repaint();
        });

        btnRow.add(saveBtn);
        btnRow.add(resetBtn);
        centre.add(btnRow);
        centre.add(Box.createVerticalStrut(24));

        add(centre, BorderLayout.CENTER);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));
        card.setMaximumSize(new Dimension(560, 999));
        card.setPreferredSize(new Dimension(540, 370));

        masterSlider = makeSlider((int) (settings.getMasterVolume() * 100));
        masterSlider.addChangeListener(e -> {
            float v = masterSlider.getValue() / 100f;
            settings.setMasterVolume(v);
            audio.setMasterVolume(v);
            repaint();
        });

        musicSlider = makeSlider((int) (settings.getMusicVolume() * 100));
        musicSlider.addChangeListener(e -> {
            float v = musicSlider.getValue() / 100f;
            settings.setMusicVolume(v);
            audio.setMusicVolume(v);
            repaint();
        });

        sfxSlider = makeSlider((int) (settings.getSFXVolume() * 100));
        sfxSlider.addChangeListener(e -> {
            float v = sfxSlider.getValue() / 100f;
            settings.setSFXVolume(v);
            audio.setSFXVolume(v);
            repaint();
        });

        musicToggle = makeToggle(settings.isMusicEnabled(), ACCENT_CYAN);
        musicToggle.addActionListener(e -> {
            boolean on = musicToggle.isSelected();
            settings.setMusicEnabled(on);
            audio.setMusicMuted(!on);
            musicToggle.setText(on ? "ON" : "OFF");
        });

        sfxToggle = makeToggle(settings.isSFXEnabled(), ACCENT_CYAN);
        sfxToggle.addActionListener(e -> {
            boolean on = sfxToggle.isSelected();
            settings.setSFXEnabled(on);
            audio.setSFXMuted(!on);
            sfxToggle.setText(on ? "ON" : "OFF");
        });

        fpsToggle = makeToggle(settings.isShowFPS(), ACCENT_AMBER);
        fpsToggle.addActionListener(e -> {
            boolean on = fpsToggle.isSelected();
            settings.setShowFPS(on);
            fpsToggle.setText(on ? "ON" : "OFF");
        });

        card.add(makeRow("Master Volume", ACCENT_CYAN, masterSlider, null));
        card.add(makeDivider());
        card.add(makeRow("Music Volume", ACCENT_CYAN, musicSlider, null));
        card.add(makeDivider());
        card.add(makeRow("SFX Volume", ACCENT_CYAN, sfxSlider, null));
        card.add(makeDivider());
        card.add(makeRow("Music", ACCENT_AMBER, null, musicToggle));
        card.add(makeDivider());
        card.add(makeRow("Sound FX", ACCENT_AMBER, null, sfxToggle));
        card.add(makeDivider());
        card.add(makeRow("Show FPS", ACCENT_AMBER, null, fpsToggle));

        return card;
    }

    private JPanel makeRow(String labelText, Color labelColor,
            JSlider slider, JCheckBox toggle) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        row.setMaximumSize(new Dimension(9999, 54));
        row.setPreferredSize(new Dimension(500, 54));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        lbl.setForeground(labelColor);
        lbl.setPreferredSize(new Dimension(160, 34));
        row.add(lbl, BorderLayout.WEST);

        if (slider != null) {

            JPanel sliderPanel = new JPanel(new BorderLayout(8, 0));
            sliderPanel.setOpaque(false);
            sliderPanel.add(slider, BorderLayout.CENTER);

            JLabel valLbl = new JLabel(slider.getValue() + "%");
            valLbl.setFont(new Font("Monospaced", Font.BOLD, 12));
            valLbl.setForeground(labelColor);
            valLbl.setPreferredSize(new Dimension(42, 34));
            valLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            sliderPanel.add(valLbl, BorderLayout.EAST);

            slider.addChangeListener(e -> valLbl.setText(slider.getValue() + "%"));

            row.add(sliderPanel, BorderLayout.CENTER);
        } else if (toggle != null) {
            JPanel tp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            tp.setOpaque(false);
            toggle.setPreferredSize(new Dimension(80, 34));
            tp.add(toggle);
            row.add(tp, BorderLayout.CENTER);
        }

        return row;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x1C, 0x2A, 0x3A));
        sep.setMaximumSize(new Dimension(9999, 1));
        return sep;
    }

    @Override
    protected void onResize(int w, int h) {

    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {

        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        String title = "SETTINGS";
        FontMetrics fm = g2.getFontMetrics();
        int tx = w / 2 - fm.stringWidth(title) / 2;
        g2.setColor(new Color(ACCENT_AMBER.getRed(), ACCENT_AMBER.getGreen(), ACCENT_AMBER.getBlue(), 45));
        g2.drawString(title, tx + 2, 74);
        g2.setColor(ACCENT_AMBER);
        g2.drawString(title, tx, 72);
        drawDivider(g2, 60, 88, w - 120, ACCENT_AMBER);

        int cardX = w / 2 - 287;
        int cardY = 110;
        int cardW = 574;
        int cardH = 380;

        g2.setColor(new Color(0x0D, 0x15, 0x20, 210));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 14, 14);
        g2.setColor(new Color(0x26, 0x32, 0x38, 140));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(cardX, cardY, cardW, cardH, 14, 14);
        g2.setStroke(new BasicStroke(1f));
    }

    private JSlider makeSlider(int value) {
        JSlider slider = new JSlider(0, 100, value) {
            @Override
            public void updateUI() {
                setUI(new BasicSliderUI(this) {
                    @Override
                    public void paintTrack(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        Rectangle t = trackRect;
                        int cy = t.y + t.height / 2;
                        g2.setColor(new Color(0x1C, 0x2A, 0x3A));
                        g2.fillRoundRect(t.x, cy - 3, t.width, 6, 4, 4);
                        int filled = (int) ((double) (slider.getValue() - slider.getMinimum())
                                / (slider.getMaximum() - slider.getMinimum()) * t.width);
                        g2.setColor(ACCENT_CYAN);
                        g2.fillRoundRect(t.x, cy - 3, filled, 6, 4, 4);
                    }

                    @Override
                    public void paintThumb(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        Rectangle t = thumbRect;
                        int cx = t.x + t.width / 2, cy = t.y + t.height / 2;
                        g2.setColor(new Color(ACCENT_CYAN.getRed(), ACCENT_CYAN.getGreen(),
                                ACCENT_CYAN.getBlue(), 55));
                        g2.fillOval(cx - 11, cy - 11, 22, 22);
                        g2.setColor(ACCENT_CYAN);
                        g2.fillOval(cx - 7, cy - 7, 14, 14);
                        g2.setColor(Color.WHITE);
                        g2.fillOval(cx - 3, cy - 3, 6, 6);
                    }

                    @Override
                    public void paintFocus(Graphics g) {
                    }
                });
            }
        };
        slider.setOpaque(false);
        slider.setFocusable(false);
        return slider;
    }

    private JCheckBox makeToggle(boolean selected, Color color) {
        JCheckBox cb = new JCheckBox(selected ? "ON" : "OFF", selected) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int cw = getWidth(), ch = getHeight();
                boolean on = isSelected();
                g2.setColor(on
                        ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 40)
                        : new Color(0x1C, 0x2A, 0x3A));
                g2.fillRoundRect(0, 0, cw, ch, 8, 8);
                g2.setColor(on ? color : new Color(0x37, 0x47, 0x4F));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, cw - 2, ch - 2, 8, 8);
                g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                g2.setColor(on ? color : new Color(0x54, 0x6E, 0x7A));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        cw / 2 - fm.stringWidth(getText()) / 2,
                        ch / 2 + fm.getAscent() / 2 - fm.getDescent() / 2);
                g2.dispose();
            }
        };
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        cb.setBorderPainted(false);
        cb.setContentAreaFilled(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return cb;
    }
}
