package game.ui.screens;

import game.ui.BaseScreen;
import game.ui.GameWindow;
import game.utils.AccountManager;

import game.utils.Profile;
import game.utils.ProfileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen extends BaseScreen {

    private enum Tab { LOGIN, REGISTER }
    private Tab activeTab = Tab.LOGIN;

    private JButton        loginTabBtn, regTabBtn;
    private JTextField     loginUserField, regUserField;
    private JPasswordField loginPassField, regPassField, regConfirmField;
    private JButton        loginBtn, regBtn;
    private JLabel         statusLabel;
    private JButton        guestBtn;

    private float glowPhase = 0f;
    private float cardSlide = 0f;

    private int cardX, cardY, cardW, cardH;

    public LoginScreen(GameWindow window) { super(window); }

    @Override
    protected void init() {
        setLayout(null);

        loginTabBtn = makeTabButton("LOGIN",    Tab.LOGIN);
        regTabBtn   = makeTabButton("REGISTER", Tab.REGISTER);

        loginUserField = makeField("Username", false);
        loginPassField = makePassField("Password");
        loginBtn       = createButton("▶  LOGIN", ACCENT_CYAN, this::doLogin);

        regUserField    = makeField("Username  (min 3 chars, letters/digits/_)", false);
        regPassField    = makePassField("Password  (min 4 chars)");
        regConfirmField = makePassField("Confirm Password");
        regBtn          = createButton("✔  CREATE ACCOUNT", ACCENT_PURPLE, this::doRegister);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        statusLabel.setForeground(new Color(0xEF, 0x53, 0x50));

        guestBtn = makeGuestButton();

        add(loginTabBtn);
        add(regTabBtn);
        add(loginUserField);
        add(loginPassField);
        add(loginBtn);
        add(regUserField);
        add(regPassField);
        add(regConfirmField);
        add(regBtn);
        add(statusLabel);
        add(guestBtn);

        loginPassField.addActionListener(e  -> doLogin());
        regConfirmField.addActionListener(e -> doRegister());
    }

    @Override
    protected void onResize(int w, int h) {
        cardW = Math.min(440, w - 60);
        cardH = 490;
        cardX = w/2 - cardW/2;
        cardY = h/2 - cardH/2;

        int fieldX = cardX + 36;
        int fieldW = cardW - 72;

        int tabY = cardY + 66;
        int tabW = cardW/2 - 8;

        if (loginTabBtn != null)    loginTabBtn.setBounds(cardX + 8,       tabY, tabW, 36);
        if (regTabBtn != null)      regTabBtn.setBounds(cardX + tabW + 16, tabY, tabW, 36);

        int loginY = tabY + 54;
        if (loginUserField != null) loginUserField.setBounds(fieldX, loginY, fieldW, 42);
        if (loginPassField != null) loginPassField.setBounds(fieldX, loginY + 56, fieldW, 42);
        if (loginBtn != null)       loginBtn.setBounds(fieldX, loginY + 116, fieldW, 46);

        int regY = tabY + 54;
        if (regUserField != null)    regUserField.setBounds(fieldX, regY, fieldW, 42);
        if (regPassField != null)    regPassField.setBounds(fieldX, regY + 56, fieldW, 42);
        if (regConfirmField != null) regConfirmField.setBounds(fieldX, regY + 112, fieldW, 42);
        if (regBtn != null)          regBtn.setBounds(fieldX, regY + 168, fieldW, 46);

        if (statusLabel != null)     statusLabel.setBounds(cardX + 8, cardY + cardH - 88, cardW - 16, 22);

        if (guestBtn != null)        guestBtn.setBounds(cardX + 8, cardY + cardH - 60, cardW - 16, 42);

        syncTabVisibility();
    }

    private void syncTabVisibility() {
        boolean login = activeTab == Tab.LOGIN;
        setVis(loginUserField, login);
        setVis(loginPassField, login);
        setVis(regUserField,    !login);
        setVis(regPassField,    !login);
        setVis(regConfirmField, !login);

        if (loginBtn != null) loginBtn.setVisible(login);
        if (regBtn != null)   regBtn.setVisible(!login);

        setStatus("", false);

        if (getWidth() > 0 && getHeight() > 0)
            onResize(getWidth(), getHeight());
        revalidate();
        repaint();

        cardSlide = 0.7f;
    }

    private void setVis(JComponent c, boolean v) { if (c != null) c.setVisible(v); }

    private void doLogin() {
        String user = loginUserField.getText().trim();
        String pass = new String(loginPassField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            setStatus("Please enter username and password.", true); return;
        }
        AccountManager am = engine.getAccountManager();
        switch (am.login(user, pass)) {
            case SUCCESS -> {
                loadOrCreateProfile(am.getLoggedIn().profileName);
                setStatus("Welcome back, " + user + "!", false);
                new Timer(700, e -> engine.showMainMenu()) {{ setRepeats(false); start(); }};
            }
            case WRONG_PASSWORD -> setStatus("Incorrect password.", true);
            case NOT_FOUND      -> setStatus("Account not found. Please register.", true);
        }
    }

    private void doRegister() {
        String user    = regUserField.getText().trim();
        String pass    = new String(regPassField.getPassword());
        String confirm = new String(regConfirmField.getPassword());
        if (!pass.equals(confirm)) { setStatus("Passwords do not match.", true); return; }

        AccountManager am = engine.getAccountManager();
        switch (am.register(user, pass)) {
            case SUCCESS -> {
                loadOrCreateProfile(user);
                setStatus("Account created! Welcome, " + user + "!", false);
                new Timer(800, e -> engine.showMainMenu()) {{ setRepeats(false); start(); }};
            }
            case USERNAME_TAKEN       -> setStatus("Username already taken.", true);
            case USERNAME_TOO_SHORT   -> setStatus("Username must be at least 3 characters.", true);
            case PASSWORD_TOO_SHORT   -> setStatus("Password must be at least 4 characters.", true);
            case USERNAME_INVALID     -> setStatus("Username: letters, numbers and _ only.", true);
            case MAX_ACCOUNTS_REACHED -> setStatus("Max 10 accounts reached.", true);
        }
    }

    private void doGuestLogin() {
        Profile guest = new Profile("Guest");
        guest.setGuest(true);
        engine.getProfileManager().setGuestProfile(guest);
        engine.showMainMenu();
    }

    private void loadOrCreateProfile(String name) {
        ProfileManager pm = engine.getProfileManager();
        Profile found = pm.getProfiles().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
        if (found == null) {
            if (pm.getProfiles().size() < 3) {
                found = new Profile(name);
                pm.getProfiles().add(found);
                pm.saveProfiles();
            } else {
                found = pm.getProfiles().get(0);
            }
        }
        found.setGuest(false);
        pm.setActiveProfile(found);
    }

    private void setStatus(String msg, boolean error) {
        if (statusLabel == null) return;
        statusLabel.setText(msg.isEmpty() ? " " : msg);
        statusLabel.setForeground(error
                ? new Color(0xFF, 0x52, 0x52)
                : new Color(0x69, 0xF0, 0xAE));
    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        glowPhase += 0.04f;
        if (cardSlide < 1f) cardSlide = Math.min(1f, cardSlide + 0.04f);
        float ease = 1f - (1f - cardSlide) * (1f - cardSlide);
        int   dy   = (int)((1f - ease) * 60);
        drawCard(g2, cardX, cardY + dy, cardW, cardH);
        drawCardContent(g2, cardX, cardY + dy, cardW, cardH);
    }

    private void drawCard(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(0,0,0,80));
        g2.fillRoundRect(x+6, y+8, w, h, 20, 20);

        g2.setColor(new Color(0x0D, 0x18, 0x2A));
        g2.fillRoundRect(x, y, w, h, 20, 20);

        float ga = 0.3f + 0.2f*(float)Math.sin(glowPhase);
        g2.setColor(new Color(ACCENT_CYAN.getRed(), ACCENT_CYAN.getGreen(),
                ACCENT_CYAN.getBlue(), (int)(ga*255)));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        GradientPaint bar = new GradientPaint(x, y, ACCENT_CYAN, x+w, y, ACCENT_PURPLE);
        g2.setPaint(bar);
        g2.fillRoundRect(x, y, w, 5, 4, 4);
    }

    private void drawCardContent(Graphics2D g2, int x, int y, int w, int h) {

        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(ACCENT_CYAN);
        String logo = "⏱  TIME LOOP ESCAPE";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(logo, x+w/2-fm.stringWidth(logo)/2, y+28);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT_DIM);
        String sub = "— ESCAPE THE LOOP OR RELIVE IT FOREVER —";
        fm = g2.getFontMetrics();
        g2.drawString(sub, x+w/2-fm.stringWidth(sub)/2, y+44);

        g2.setColor(new Color(ACCENT_CYAN.getRed(),ACCENT_CYAN.getGreen(),ACCENT_CYAN.getBlue(),40));
        g2.fillRect(x+20, y+54, w-40, 1);

        int tabW  = w/2 - 8;
        int tabY  = y + 66;
        int actX  = activeTab == Tab.LOGIN ? x+8 : x+tabW+16;
        g2.setColor(ACCENT_CYAN);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(actX, tabY+36, actX+tabW, tabY+36);
        g2.setStroke(new BasicStroke(1f));

        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT_DIM);
        String hint = activeTab == Tab.LOGIN
                ? "Enter your credentials to continue"
                : "Create a new account to save your progress";
        fm = g2.getFontMetrics();
        g2.drawString(hint, x+w/2-fm.stringWidth(hint)/2, tabY+50);

        int fieldLabelX = x+36;
        int sY = tabY+64;
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        Color fc = activeTab == Tab.LOGIN ? ACCENT_CYAN : ACCENT_PURPLE;
        if (activeTab == Tab.LOGIN) {
            drawFieldLabel(g2, "USERNAME", fieldLabelX, sY-4,   fc);
            drawFieldLabel(g2, "PASSWORD", fieldLabelX, sY+52,  fc);
        } else {
            drawFieldLabel(g2, "USERNAME", fieldLabelX, sY-4,   fc);
            drawFieldLabel(g2, "PASSWORD", fieldLabelX, sY+52,  fc);
            drawFieldLabel(g2, "CONFIRM",  fieldLabelX, sY+108, fc);
        }

        g2.setColor(new Color(0x1C,0x2A,0x3A));
        g2.fillRect(x+20, y+h-98, w-40, 1);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(0x54,0x6E,0x7A));
        String note = "Guest progress will not be saved";
        fm = g2.getFontMetrics();
        g2.drawString(note, x+w/2-fm.stringWidth(note)/2, y+h-70);
    }

    private void drawFieldLabel(Graphics2D g2, String text, int x, int y, Color c) {
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 160));
        g2.drawString(text, x, y);
    }

    private JTextField makeField(String placeholder, boolean ignored) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x08,0x10,0x20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isFocusOwner()
                        ? new Color(ACCENT_CYAN.getRed(),ACCENT_CYAN.getGreen(),ACCENT_CYAN.getBlue(),160)
                        : new Color(0x1C,0x2A,0x3A));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleField(field, placeholder);
        return field;
    }

    private JPasswordField makePassField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x08,0x10,0x20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isFocusOwner()
                        ? new Color(ACCENT_CYAN.getRed(),ACCENT_CYAN.getGreen(),ACCENT_CYAN.getBlue(),160)
                        : new Color(0x1C,0x2A,0x3A));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleField(field, placeholder);
        return field;
    }

    private void styleField(JTextField field, String placeholder) {
        field.setFont(new Font("Monospaced", Font.PLAIN, 14));
        field.setForeground(TEXT_DIM);
        field.setCaretColor(ACCENT_CYAN);
        field.setBackground(new Color(0x08,0x10,0x20));
        field.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        field.setOpaque(false);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                    if (field instanceof JPasswordField pf) pf.setEchoChar('•');
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_DIM);
                    if (field instanceof JPasswordField pf) pf.setEchoChar((char)0);
                }
            }
        });
        if (field instanceof JPasswordField pf) pf.setEchoChar((char)0);
    }

    private JButton makeTabButton(String label, Tab tab) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = activeTab == tab;
                g2.setColor(active
                        ? new Color(ACCENT_CYAN.getRed(),ACCENT_CYAN.getGreen(),ACCENT_CYAN.getBlue(),22)
                        : new Color(0,0,0,0));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                g2.setColor(active ? ACCENT_CYAN : TEXT_DIM);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        getWidth()/2-fm.stringWidth(getText())/2,
                        getHeight()/2+fm.getAscent()/2-fm.getDescent()/2);
                g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            System.out.println("[LoginScreen] Tab clicked: " + tab);
            activeTab = tab;
            syncTabVisibility();
        });
        return btn;
    }

    private JButton makeGuestButton() {
        JButton btn = new JButton("👤  PLAY AS GUEST") {
            private boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent  e) { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(new Color(0x54,0x6E,0x7A,30));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                }
                g2.setColor(new Color(0x37,0x47,0x4F));
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0, new float[]{5f,4f}, 0f));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,8,8);
                g2.setStroke(new BasicStroke(1f));
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.setColor(hovered ? new Color(0x78,0x90,0x9C) : new Color(0x54,0x6E,0x7A));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        getWidth()/2-fm.stringWidth(getText())/2,
                        getHeight()/2+fm.getAscent()/2-fm.getDescent()/2);
                g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> doGuestLogin());
        return btn;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_TAB) {
            activeTab = (activeTab == Tab.LOGIN) ? Tab.REGISTER : Tab.LOGIN;
            syncTabVisibility();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {

            if (activeTab == Tab.LOGIN)    doLogin();
            else                           doRegister();
        }
    }
}
