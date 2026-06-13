package game.ui.screens;

import game.animations.AnimationSystem;
import game.audio.AudioManager;
import game.core.GameSession;
import game.entities.Interactable;
import game.entities.Item;
import game.entities.Player;
import game.entities.Room;
import game.ui.BaseScreen;
import game.ui.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class GameScreen extends BaseScreen {

    private enum Phase {
        PLAYING,
        LOOP_RESETTING,
        ROOM_TRANSITION,
        WINNING,
        PAUSED
    }
    private Phase phase = Phase.PLAYING;

    private final GameSession     session;
    private final AnimationSystem anim;
    private       Room            currentRoom;
    private       int             roomIndex = 0;

    private Timer   gameTimer;
    private long    lastMs;

    private int     camX, camY;

    private String  dlgTitle, dlgText;
    private float   dlgTimer = 0f;

    private boolean kUp, kDown, kLeft, kRight, kShift;
    private float   moveDelay = 0f;

    private int lastTickSec = -1;

    private float transitionTimer = 0f;

    public GameScreen(GameWindow window, GameSession session) {
        super(window);
        this.session     = session;
        this.anim        = new AnimationSystem();
        this.currentRoom = session.getRooms().get(0);
        buildCamera();
        startGameTimer();
        showDlg("LOOP #1", "You are trapped.\nFind clues. Reach the portal.");
    }

    @Override
    protected void init() {
        setLayout(null);

        JButton pb = new JButton("⏸");
        pb.setFont(new Font("SansSerif", Font.PLAIN, 18));
        pb.setForeground(TEXT_DIM);
        pb.setBackground(new Color(0x0D, 0x15, 0x20));
        pb.setBorder(BorderFactory.createLineBorder(new Color(0x26, 0x32, 0x38)));
        pb.setFocusPainted(false);
        pb.addActionListener(e -> togglePause());
        add(pb);
    }

    @Override
    protected void onResize(int w, int h) {

        for (Component c : getComponents()) {
            if (c instanceof JButton) c.setBounds(w - 56, 8, 44, 36);
        }
        buildCamera();
    }

    private void startGameTimer() {
        lastMs    = System.currentTimeMillis();
        gameTimer = new Timer(16, e -> tick());
        gameTimer.start();
    }

    private void tick() {
        long  now = System.currentTimeMillis();
        float dt  = Math.min((now - lastMs) / 1000f, 0.05f);
        lastMs = now;

        switch (phase) {
            case PLAYING          -> updatePlaying(dt);
            case LOOP_RESETTING   -> updateTransition(dt, false);
            case ROOM_TRANSITION  -> updateTransition(dt, true);
            case WINNING, PAUSED  -> {  }
        }
        repaint();
    }

    private void updatePlaying(float dt) {

        if (session.getRemainingSeconds() <= 0) {
            startLoopReset();
            return;
        }

        moveDelay -= dt;
        if (moveDelay <= 0) {
            session.getPlayer().setSprinting(kShift && session.getPlayer().getSanity() > 0);
            handleMovement();
            moveDelay = 0.15f;
        }

        session.getPlayer().update(dt);
        currentRoom.update(dt);
        anim.update(dt);

        if (dlgTimer > 0) {
            dlgTimer -= dt;
            if (dlgTimer <= 0) { dlgTitle = null; dlgText = null; }
        }

        int secs = session.getRemainingSeconds();
        if (secs > 0 && secs <= 10 && secs != lastTickSec) {
            lastTickSec = secs;
            audio.playSFX(AudioManager.SFX.TICK);
        }

        Player.SanityLevel sl = session.getPlayer().getSanityLevel();
        if (sl == Player.SanityLevel.PARANOID || sl == Player.SanityLevel.BREAKING)
            anim.triggerGlitch(0.004f);
    }

    private void updateTransition(float dt, boolean isRoomTransition) {
        anim.update(dt);
        if (dlgTimer > 0) dlgTimer -= dt;

        transitionTimer -= dt;
        if (transitionTimer <= 0) {
            phase = Phase.PLAYING;
            System.out.println("[Game] Transition ended → PLAYING  room="
                    + currentRoom.getName() + "  secs=" + session.getRemainingSeconds());
        }
    }

    private void handleMovement() {
        Player p = session.getPlayer();
        if (p.isMoving()) return;

        int dx = 0, dy = 0;
        Player.Direction dir = p.getFacing();
        if (kUp)    { dy = -1; dir = Player.Direction.UP;    }
        if (kDown)  { dy =  1; dir = Player.Direction.DOWN;  }
        if (kLeft)  { dx = -1; dir = Player.Direction.LEFT;  }
        if (kRight) { dx =  1; dir = Player.Direction.RIGHT; }
        if (dx == 0 && dy == 0) return;

        int nx = p.getGridX() + dx;
        int ny = p.getGridY() + dy;

        if (!currentRoom.isWalkable(nx, ny)) return;

        if (currentRoom.getTile(nx, ny) == Room.TILE_EXIT) {
            advanceRoom();
            return;
        }

        p.moveTo(nx, ny, dir);

        Item item = currentRoom.getItemAt(nx, ny);
        if (item != null && !item.isCollected()) {
            item.collect();
            session.collectItem(item);
            p.pickUp(item);
            anim.spawnPickup(camX + nx * 64 + 32, camY + ny * 64 + 32, item.getColor());
            audio.playSFX(AudioManager.SFX.CLUE_FOUND);

            showDlg(item.getName(), item.getDescription());
        }
    }

    private void tryInteract() {
        if (phase != Phase.PLAYING) return;
        Player p  = session.getPlayer();
        int gx = p.getGridX(), gy = p.getGridY();
        int[][] adj = {{gx, gy-1}, {gx, gy+1}, {gx-1, gy}, {gx+1, gy}};

        for (int[] c : adj) {
            Interactable ia = currentRoom.getInteractableAt(c[0], c[1]);
            if (ia == null) continue;

            boolean wasUnlocked = currentRoom.isExitUnlocked();
            boolean ok = ia.tryActivate(p);

            if (ok) {
                session.addScore(100);
                audio.playSFX(AudioManager.SFX.CLUE_FOUND);
                anim.spawnPickup(camX + ia.getGridX()*64+32, camY + ia.getGridY()*64+32, ACCENT_CYAN);

                if (!wasUnlocked && currentRoom.isExitUnlocked()) {

                    showDlg("EXIT UNLOCKED!", "Portal open!\nWalk to the top of the room.");
                    audio.playSFX(AudioManager.SFX.ESCAPE);
                } else {
                    showDlg(ia.getLabel(), ia.getHint());
                }
            } else {
                audio.playSFX(AudioManager.SFX.ERROR);
                showDlg("Locked", ia.getHint());
            }
            return;
        }
    }

    private void advanceRoom() {
        roomIndex++;
        System.out.println("[Game] advanceRoom → roomIndex=" + roomIndex);

        if (roomIndex >= session.getRooms().size()) {

            phase = Phase.WINNING;
            gameTimer.stop();
            session.markEscaped();
            audio.playSFX(AudioManager.SFX.ESCAPE);
            anim.spawnEscape(GameWindow.WIDTH / 2, GameWindow.HEIGHT / 2);
            new Timer(2500, e -> engine.showVictory(session)) {{
                setRepeats(false); start();
            }};
        } else {

            session.onRoomAdvance();

            currentRoom = session.getRooms().get(roomIndex);

            anim.clear();

            lastTickSec = -1;

            session.getPlayer().setGridPosition(
                    currentRoom.getGridW() / 2,
                    currentRoom.getGridH() - 2);

            buildCamera();

            showDlg("ROOM " + (roomIndex + 1),
                    currentRoom.getName() + "\nFind clues. Reach the portal.");

            phase = Phase.ROOM_TRANSITION;
            transitionTimer = 1.0f;

            System.out.println("[Game] Entering: " + currentRoom.getName()
                    + "  camX=" + camX + " camY=" + camY
                    + "  timer=" + session.getRemainingSeconds() + "s");
        }
    }

    private void startLoopReset() {
        if (phase == Phase.LOOP_RESETTING || phase == Phase.WINNING) return;

        session.triggerLoopReset();

        if (session.isGameOver()) {
            phase = Phase.WINNING;
            gameTimer.stop();
            audio.playSFX(AudioManager.SFX.LOOP_RESET);
            new Timer(1500, e -> engine.showGameOver(session)) {{
                setRepeats(false); start();
            }};
            return;
        }

        roomIndex   = 0;
        currentRoom = session.getRooms().get(0);
        lastTickSec = -1;

        session.getPlayer().setGridPosition(1, 1);

        buildCamera();

        anim.clear();
        anim.spawnLoopReset(GameWindow.WIDTH / 2, GameWindow.HEIGHT / 2);
        audio.playSFX(AudioManager.SFX.LOOP_RESET);

        showDlg("LOOP #" + (session.getLoopCount() + 1),
                "The loop has reset.\nSanity decreasing...");

        phase = Phase.LOOP_RESETTING;
        transitionTimer = 1.0f;

        System.out.println("[Game] Loop reset → loop=" + session.getLoopCount()
                + "  timer restarted to " + session.getRemainingSeconds() + "s");
    }

    private void togglePause() {
        if (phase == Phase.PLAYING) {
            phase = Phase.PAUSED;
            audio.pauseMusic();
        } else if (phase == Phase.PAUSED) {
            phase = Phase.PLAYING;
            audio.resumeMusic();
        }
    }

    private void buildCamera() {
        int ww = GameWindow.WIDTH;
        int wh = GameWindow.HEIGHT;

        if (getWidth()  > 200) ww = getWidth();
        if (getHeight() > 200) wh = getHeight();

        int rw = currentRoom.getGridW() * currentRoom.getTileSize();
        int rh = currentRoom.getGridH() * currentRoom.getTileSize();

        camX = (ww - rw) / 2;
        camY = (wh - rh) / 2 + 24;

        if (camX < 4)  camX = 4;
        if (camY < 56) camY = 56;

        System.out.println("[Camera] ww=" + ww + " wh=" + wh
                + " camX=" + camX + " camY=" + camY);
    }

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        currentRoom.draw(g2, camX, camY);

        Graphics2D gp = (Graphics2D) g2.create();
        gp.translate(camX, camY);
        session.getPlayer().draw(gp);
        gp.dispose();

        anim.draw(g2, w, h);
        drawHUD(g2, w, h);

        if (phase == Phase.LOOP_RESETTING || phase == Phase.ROOM_TRANSITION) {
            float alpha = Math.min(0.35f, transitionTimer * 0.4f);
            g2.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
            g2.fillRect(0, 0, w, h);
        }

        if (dlgText != null && dlgTimer > 0) drawDialogue(g2, w, h);
        if (phase == Phase.PAUSED)            drawPause(g2, w, h);
    }

    private void drawHUD(Graphics2D g2, int w, int h) {

        g2.setColor(new Color(5, 10, 18, 215));
        g2.fillRect(0, 0, w, 52);
        g2.setColor(new Color(0, 188, 212, 60));
        g2.fillRect(0, 50, w, 2);

        int   secs = session.getRemainingSeconds();
        Color tc   = secs <= 10 ? new Color(0xFF, 0x52, 0x52)
                   : secs <= 30 ? new Color(0xFF, 0xB3, 0x00) : ACCENT_CYAN;
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        g2.setColor(tc);
        g2.drawString(String.format("⏱ %02d:%02d", secs / 60, secs % 60), 16, 34);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(TEXT_DIM);
        g2.drawString("LOOP " + (session.getLoopCount()+1) + "/" + GameSession.MAX_LOOPS, 210, 20);

        int cluesHave = session.getClueCount();
        g2.setColor(cluesHave > 0 ? ACCENT_AMBER : TEXT_DIM);
        g2.drawString("🔍 " + cluesHave + " CLUES", 210, 38);

        g2.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2.setColor(ACCENT_CYAN);
        String sc = "SCORE  " + session.getScore();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(sc, w/2 - fm.stringWidth(sc)/2, 32);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(TEXT_DIM);
        g2.drawString(currentRoom.getName(), w - 215, 20);
        Color dc = session.getDifficulty().accentColor;
        g2.setColor(new Color(dc.getRed(), dc.getGreen(), dc.getBlue(), 140));
        g2.fillRoundRect(w-170, 24, 150, 22, 6, 6);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        g2.drawString(session.getDifficulty().label, w-158, 40);

        drawSanityBar(g2, h);
        drawInventory(g2, w, h);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(0x37, 0x47, 0x4F));
        g2.drawString("WASD: Move  •  E: Interact  •  ESC: Pause", 16, h - 10);
    }

    private void drawSanityBar(Graphics2D g2, int h) {
        int san = session.getPlayer().getSanity();
        int bx = 16, by = h-28, bw = 100, bh = 8;
        g2.setColor(new Color(0x1C, 0x2A, 0x3A));
        g2.fillRoundRect(bx, by, bw, bh, 4, 4);
        Color sc = san>60 ? new Color(0x4C,0xAF,0x50)
                 : san>30 ? new Color(0xFF,0xB3,0x00)
                          : new Color(0xF4,0x43,0x36);
        g2.setColor(sc);
        g2.fillRoundRect(bx, by, (int)(bw*san/100f), bh, 4, 4);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT_DIM);
        g2.drawString("SANITY " + san + "%", bx, by-3);
    }

    private void drawInventory(Graphics2D g2, int w, int h) {
        List<Item> inv = session.getPlayer().getInventory();
        int sz = 44, iy = h - 58;
        int sx = w/2 - (Math.max(1, inv.size()) * (sz+4)) / 2;
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT_DIM);
        g2.drawString("INVENTORY", sx, iy-4);
        for (int i = 0; i < inv.size(); i++) {
            int ix = sx + i*(sz+4);
            g2.setColor(new Color(0x0D,0x15,0x20));
            g2.fillRoundRect(ix, iy, sz, sz, 6, 6);
            g2.setColor(inv.get(i).getColor());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(ix, iy, sz, sz, 6, 6);
            g2.setStroke(new BasicStroke(1f));
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            String lbl = inv.get(i).getName().substring(0, Math.min(3, inv.get(i).getName().length()));
            g2.drawString(lbl, ix+4, iy+sz/2+4);
        }
    }

    private void drawDialogue(Graphics2D g2, int w, int h) {
        int dw = Math.min(620, w-40), dh = 120;
        int dx = w/2 - dw/2, dy = h - 190;
        float fade = Math.min(1f, dlgTimer / 0.4f);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        g2.setColor(new Color(5,10,20,230));
        g2.fillRoundRect(dx, dy, dw, dh, 12, 12);
        g2.setColor(new Color(0,188,212,140));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(dx, dy, dw, dh, 12, 12);
        g2.setStroke(new BasicStroke(1f));
        if (dlgTitle != null) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.setColor(ACCENT_CYAN);
            g2.drawString(dlgTitle, dx+16, dy+24);
        }
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(TEXT_PRIMARY);
        String[] lines = dlgText.split("\n");
        for (int i = 0; i < lines.length; i++)
            g2.drawString(lines[i], dx+16, dy+46+i*20);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawPause(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0,0,0,160));
        g2.fillRect(0,0,w,h);
        g2.setFont(new Font("Monospaced",Font.BOLD,48));
        g2.setColor(ACCENT_CYAN);
        String ps = "PAUSED";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(ps, w/2-fm.stringWidth(ps)/2, h/2-20);
        g2.setFont(new Font("Monospaced",Font.PLAIN,16));
        g2.setColor(TEXT_DIM);
        String hint = "ESC to resume  •  M for Main Menu";
        g2.drawString(hint, w/2-g2.getFontMetrics().stringWidth(hint)/2, h/2+24);
    }

    private void showDlg(String title, String text) {
        dlgTitle = title; dlgText = text; dlgTimer = 5f;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> kUp    = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> kDown  = true;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> kLeft  = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> kRight = true;
            case KeyEvent.VK_SHIFT                -> kShift = true;
            case KeyEvent.VK_E                    -> tryInteract();
            case KeyEvent.VK_ESCAPE               -> togglePause();
            case KeyEvent.VK_M -> { if (phase == Phase.PAUSED) engine.showMainMenu(); }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> kUp    = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> kDown  = false;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> kLeft  = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> kRight = false;
            case KeyEvent.VK_SHIFT                -> kShift = false;
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (gameTimer != null) gameTimer.stop();
    }
}
