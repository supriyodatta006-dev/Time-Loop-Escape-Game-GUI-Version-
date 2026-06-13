package game.ui;

import game.audio.AudioManager;
import game.core.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class BaseScreen extends JPanel
        implements KeyListener, MouseListener, MouseMotionListener {

    public static final Color BG_DARK       = new Color(0x080C14);
    public static final Color BG_PANEL      = new Color(0x0D1520);
    public static final Color ACCENT_CYAN   = new Color(0x00BCD4);
    public static final Color ACCENT_PURPLE = new Color(0x7C4DFF);
    public static final Color ACCENT_AMBER  = new Color(0xFFB300);
    public static final Color TEXT_PRIMARY  = new Color(0xE8EAF6);
    public static final Color TEXT_DIM      = new Color(0x78909C);

    public static final Font FONT_TITLE  = new Font("Monospaced", Font.BOLD, 42);
    public static final Font FONT_H2     = new Font("Monospaced", Font.BOLD, 22);
    public static final Font FONT_BODY   = new Font("SansSerif",  Font.PLAIN, 15);
    public static final Font FONT_SMALL  = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Monospaced", Font.BOLD,  16);

    protected final GameWindow   window;
    protected final GameEngine   engine;
    protected final AudioManager audio;

    private float bgPhase = 0f;
    private Timer bgTimer;
    private Timer resizeDebounce;

    protected BaseScreen(GameWindow window) {
        this.window = window;
        this.engine = GameEngine.getInstance();
        this.audio  = AudioManager.getInstance();

        setLayout(null);
        setOpaque(true);
        setBackground(BG_DARK);
        setFocusable(true);
        setDoubleBuffered(true);

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (resizeDebounce != null && resizeDebounce.isRunning())
                    resizeDebounce.stop();
                resizeDebounce = new Timer(80, ev -> {
                    resizeDebounce.stop();
                    if (getWidth() > 0 && getHeight() > 0) {
                        onResize(getWidth(), getHeight());
                        revalidate();
                        repaint();
                    }
                });
                resizeDebounce.setRepeats(false);
                resizeDebounce.start();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (getWidth() > 0 && getHeight() > 0) {
                        onResize(getWidth(), getHeight());
                        revalidate();
                        repaint();
                    }
                });
            }
        });

        init();

        bgTimer = new Timer(50, e -> {
            bgPhase += 0.025f;
            repaint();
        });
        bgTimer.setInitialDelay(100);
        bgTimer.start();
    }

    protected abstract void init();

    protected void onResize(int w, int h) {}

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
            if (getWidth() > 0 && getHeight() > 0) {
                onResize(getWidth(), getHeight());
                revalidate();
                repaint();
            }
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (bgTimer != null)        { bgTimer.stop();        bgTimer = null; }
        if (resizeDebounce != null) { resizeDebounce.stop(); resizeDebounce = null; }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getWidth() == 0 || getHeight() == 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth(), h = getHeight();

            g2.setColor(new Color(0x05, 0x0A, 0x12));
            g2.fillRect(0, 0, w, h);
            g2.setPaint(new GradientPaint(0, 0, new Color(0x08, 0x10, 0x22),
                                          w, h, new Color(0x02, 0x06, 0x10)));
            g2.fillRect(0, 0, w, h);

            drawStarField(g2, w, h);
            drawScanlines(g2, w, h);
            drawScreen(g2, w, h);
        } finally {
            g2.dispose();
        }
    }

    private void drawStarField(Graphics2D g2, int w, int h) {
        for (int i = 0; i < 40; i++) {
            float seed  = i * 137.508f;
            int   x     = Math.abs((int)(seed * 73.1f)   % w);
            int   y     = Math.abs((int)(seed * 131.7f)  % h);
            float alpha = 0.12f + 0.18f * (float) Math.abs(Math.sin(bgPhase + i * 0.31f));
            int   sz    = (i % 8 == 0) ? 2 : 1;
            g2.setColor(new Color(1f, 1f, 1f, Math.min(1f, alpha)));
            g2.fillRect(x, y, sz, sz);
        }
    }

    private void drawScanlines(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 15));
        for (int y = 0; y < h; y += 6) g2.fillRect(0, y, w, 1);
    }

    protected void drawScreen(Graphics2D g2, int w, int h) {}

    protected JButton createButton(String text, Color accentColor, Runnable action) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            private float   glowAmt = 0f;
            private Timer   glowTimer;

            {
                setFont(FONT_BUTTON);
                setForeground(accentColor);
                setBackground(new Color(0x0D, 0x15, 0x20));
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        hovered = true; animateGlow(true);
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        hovered = false; animateGlow(false);
                    }
                    @Override public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            audio.playSFX(AudioManager.SFX.CLICK);
                            action.run();
                        }
                    }
                });
            }

            private void animateGlow(boolean in) {
                if (glowTimer != null) glowTimer.stop();
                glowTimer = new Timer(16, null);
                glowTimer.addActionListener(ae -> {
                    glowAmt += in ? 0.12f : -0.12f;
                    glowAmt  = Math.max(0f, Math.min(1f, glowAmt));
                    repaint();
                    if ((in && glowAmt >= 1f) || (!in && glowAmt <= 0f)) glowTimer.stop();
                });
                glowTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                if (hovered && glowAmt > 0) {
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                            accentColor.getBlue(), (int)(glowAmt * 35)));
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                }
                if (glowAmt > 0) {
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                            accentColor.getBlue(), (int)(glowAmt * 90)));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, w - 2, h - 2, 8, 8);
                }
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                        accentColor.getBlue(), 180 + (int)(glowAmt * 75)));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 8, 8);

                g2.setFont(FONT_BUTTON);
                g2.setColor(hovered ? Color.WHITE : accentColor);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        w / 2 - fm.stringWidth(getText()) / 2,
                        h / 2 + fm.getAscent() / 2 - fm.getDescent() / 2 - 1);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(FONT_BUTTON);
                return new Dimension(fm.stringWidth(getText()) + 60, 48);
            }
        };
        return btn;
    }

    protected void drawLabel(Graphics2D g2, String text, int x, int y, Color color) {
        g2.setFont(FONT_H2);
        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(color);
        g2.drawString(text, x, y);
    }

    protected void drawDivider(Graphics2D g2, int x, int y, int w, Color color) {
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x, y, x + w, y);
        g2.setStroke(new BasicStroke(1f));
    }

    @Override public void keyPressed(KeyEvent e)     {}
    @Override public void keyReleased(KeyEvent e)    {}
    @Override public void keyTyped(KeyEvent e)       {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e){}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e)  {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e)   {}
}
