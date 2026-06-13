package game.entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Room {

    public static final int TILE_FLOOR  = 0;
    public static final int TILE_WALL   = 1;
    public static final int TILE_DOOR   = 2;
    public static final int TILE_PUZZLE = 3;
    public static final int TILE_EXIT   = 4;

    private final String  id, name;
    private final int     gridW, gridH, tileSize;
    private final int[][] tiles;

    private final List<Item>         items         = new ArrayList<>();
    private final List<Interactable> interactables = new ArrayList<>();

    private Color floorColor  = new Color(0x26, 0x32, 0x38);
    private Color wallColor   = new Color(0x1A, 0x23, 0x7E);
    private Color accentColor = new Color(0x00, 0xBC, 0xD4);

    private boolean exitUnlocked = false;
    private float   ambientPhase = 0f;

    public Room(String id, String name, int gridW, int gridH, int tileSize) {
        this.id = id; this.name = name;
        this.gridW = gridW; this.gridH = gridH; this.tileSize = tileSize;
        this.tiles = new int[gridH][gridW];
        buildDefaultLayout();
    }

    private void buildDefaultLayout() {
        for (int r = 0; r < gridH; r++)
            for (int c = 0; c < gridW; c++)
                tiles[r][c] = (r==0||r==gridH-1||c==0||c==gridW-1) ? TILE_WALL : TILE_FLOOR;
        tiles[0][gridW/2] = TILE_EXIT;
    }

    public void setTile(int col, int row, int type) {
        if (row>=0&&row<gridH&&col>=0&&col<gridW) tiles[row][col] = type;
    }
    public int getTile(int col, int row) {
        if (row<0||row>=gridH||col<0||col>=gridW) return TILE_WALL;
        return tiles[row][col];
    }
    public boolean isWalkable(int col, int row) {
        int t = getTile(col, row);
        return t != TILE_WALL && !(t == TILE_EXIT && !exitUnlocked);
    }

    public void update(float dt) {
        ambientPhase += dt * 0.6f;
        items.forEach(i -> i.update(dt));
        interactables.forEach(ia -> ia.update(dt));
    }

    public void draw(Graphics2D g2, int offX, int offY) {
        Graphics2D gc = (Graphics2D) g2.create();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        for (int r = 0; r < gridH; r++)
            for (int c = 0; c < gridW; c++)
                if (tiles[r][c] == TILE_FLOOR)
                    drawFloor(gc, offX + c*tileSize, offY + r*tileSize, c, r);

        for (int r = 0; r < gridH; r++) {
            for (int c = 0; c < gridW; c++) {
                int px = offX + c*tileSize, py = offY + r*tileSize;
                switch (tiles[r][c]) {
                    case TILE_WALL   -> drawWall(gc, px, py, c, r);
                    case TILE_DOOR   -> drawDoor(gc, px, py);
                    case TILE_PUZZLE -> drawPuzzle(gc, px, py, c, r);
                    case TILE_EXIT   -> drawExit(gc, px, py);
                }
            }
        }

        drawAmbientBloom(gc, offX, offY);

        Graphics2D gt = (Graphics2D) gc.create();
        gt.translate(offX, offY);
        items.forEach(i -> i.draw(gt, tileSize));
        interactables.forEach(ia -> ia.draw(gt, tileSize));
        gt.dispose();

        gc.dispose();
    }

    private void drawFloor(Graphics2D g2, int px, int py, int col, int row) {
        int ts = tileSize;

        boolean alt = (col + row) % 2 == 0;
        Color base = alt
                ? floorColor
                : new Color(Math.max(0, floorColor.getRed()-8),
                            Math.max(0, floorColor.getGreen()-8),
                            Math.max(0, floorColor.getBlue()-8));
        g2.setColor(base);
        g2.fillRect(px, py, ts, ts);

        GradientPaint light = new GradientPaint(
                px, py,       new Color(255,255,255, alt ? 18 : 10),
                px+ts, py+ts, new Color(0,0,0, 30));
        g2.setPaint(light);
        g2.fillRect(px, py, ts, ts);

        long seed = col * 1000L + row;
        if ((seed * 37 + 11) % 7 == 0) {
            g2.setColor(new Color(0,0,0,25));
            g2.setStroke(new BasicStroke(0.8f));
            int x1=(int)(px+6+(seed*13)%20), y1=(int)(py+8+(seed*7)%20);
            int x2=(int)(x1+4+(seed*3)%14), y2=(int)(y1+3+(seed*11)%14);
            g2.drawLine(x1,y1,x2,y2);
            g2.setStroke(new BasicStroke(1f));
        }

        g2.setColor(new Color(0,0,0, 55));
        g2.drawRect(px, py, ts, ts);

        g2.setColor(new Color(255,255,255, 10));
        g2.drawRect(px+2, py+2, ts-4, ts-4);
        g2.setColor(new Color(0,0,0, 20));
        g2.drawRect(px+3, py+3, ts-6, ts-6);

        boolean nearSpecial = isNearSpecial(col, row);
        if (nearSpecial) {
            float dist = nearSpecialDist(col, row);
            float glow = Math.max(0, 1f - dist/3f);
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                    accentColor.getBlue(), (int)(glow * 22)));
            g2.fillRect(px, py, ts, ts);
        }
    }

    private Color adjustColor(Color base, int dr, int dg, int db) {
        int r = Math.max(0, Math.min(255, base.getRed() + dr));
        int g = Math.max(0, Math.min(255, base.getGreen() + dg));
        int b = Math.max(0, Math.min(255, base.getBlue() + db));
        return new Color(r, g, b);
    }

    private void drawWall(Graphics2D g2, int px, int py, int col, int row) {
        int ts = tileSize;

        Color darkBase = new Color(
                Math.max(0, wallColor.getRed()   / 6),
                Math.max(0, wallColor.getGreen() / 6),
                Math.max(0, wallColor.getBlue()  / 6));
        g2.setColor(darkBase);
        g2.fillRect(px, py, ts, ts);

        GradientPaint face = new GradientPaint(
                px, py,    adjustColor(wallColor, 30, 30, 40),
                px, py+ts, adjustColor(wallColor, -15, -15, -10));
        g2.setPaint(face);
        g2.fillRect(px+2, py, ts-2, ts-4);

        g2.setColor(new Color(255,255,255,45));
        g2.fillRect(px, py, ts, 3);

        g2.setColor(new Color(255,255,255,25));
        g2.fillRect(px, py, 3, ts);

        g2.setColor(new Color(0,0,0,90));
        g2.fillRect(px, py+ts-4, ts, 4);

        g2.setColor(new Color(0,0,0,60));
        g2.fillRect(px+ts-3, py, 3, ts);

        float edgeGlow = 0.45f + 0.35f*(float)Math.sin(ambientPhase + col*0.4f + row*0.6f);
        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                accentColor.getBlue(), (int)(65*edgeGlow)));
        g2.fillRect(px, py+ts-3, ts, 3);

        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                accentColor.getBlue(), (int)(30*edgeGlow)));
        g2.fillRect(px, py, 2, ts);

        long seed = col*31L + row*17L;
        if ((seed % 5) == 0) {
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                    accentColor.getBlue(), 35));
            g2.setStroke(new BasicStroke(1f));
            int lx = px + (int)(seed % 40) + 4;
            g2.drawLine(lx, py+6, lx, py+ts-8);
            if ((seed % 3) == 0) g2.drawLine(lx, py+ts/2, lx+10, py+ts/2);
            g2.setStroke(new BasicStroke(1f));
        }

        if ((seed % 7) == 1) {
            g2.setColor(new Color(
                    Math.min(255, wallColor.getRed()   + 60),
                    Math.min(255, wallColor.getGreen() + 60),
                    Math.min(255, wallColor.getBlue()  + 60), 30));
            g2.setStroke(new BasicStroke(0.7f));
            int ly = py + (int)((seed * 13) % (ts - 16)) + 8;
            g2.drawLine(px+4, ly, px+ts-4, ly);
            g2.setStroke(new BasicStroke(1f));
        }

        boolean wallLeft  = col > 0        && tiles[row][col-1] == TILE_WALL;
        boolean wallRight = col < gridW-1  && tiles[row][col+1] == TILE_WALL;
        boolean wallUp    = row > 0        && tiles[row-1][col] == TILE_WALL;
        boolean wallDown  = row < gridH-1  && tiles[row+1][col] == TILE_WALL;

        if ((!wallLeft || !wallRight) && (!wallUp || !wallDown)) {
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                    accentColor.getBlue(), 60));
            g2.fillRect(px, py, 5, 5);
            g2.fillRect(px+ts-5, py, 5, 5);
            g2.fillRect(px, py+ts-5, 5, 5);
            g2.fillRect(px+ts-5, py+ts-5, 5, 5);
        }
    }

    private void drawDoor(Graphics2D g2, int px, int py) {
        int ts = tileSize;

        g2.setColor(new Color(0x3E, 0x27, 0x23));
        g2.fillRect(px, py, ts, ts);

        GradientPaint wood = new GradientPaint(
                px+6, py+4, new Color(0x6D, 0x40, 0x2A),
                px+ts-6, py+ts-4, new Color(0x4A, 0x28, 0x18));
        g2.setPaint(wood);
        g2.fillRoundRect(px+6, py+4, ts-12, ts-8, 4, 4);

        g2.setColor(new Color(0,0,0,30));
        g2.setStroke(new BasicStroke(0.8f));
        for (int i = 0; i < 4; i++) {
            int ly = py + 10 + i*10;
            g2.drawLine(px+8, ly, px+ts-8, ly);
        }
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(255,255,255,30));
        g2.drawRoundRect(px+8, py+6, ts-16, ts-12, 4, 4);
        g2.setColor(new Color(0,0,0,40));
        g2.drawRoundRect(px+9, py+7, ts-18, ts-14, 3, 3);

        g2.setColor(new Color(0xB0, 0x90, 0x60));
        g2.fillRoundRect(px+6, py+10, 7, 5, 2, 2);
        g2.fillRoundRect(px+6, py+ts-16, 7, 5, 2, 2);

        g2.setColor(new Color(0x80, 0x60, 0x20));
        g2.fillOval(px+ts/2-3, py+ts/2-4, 7, 7);
        g2.setColor(new Color(0x40, 0x30, 0x10));
        g2.fillRect(px+ts/2-1, py+ts/2+2, 3, 5);

        g2.setColor(new Color(0xC0, 0x9A, 0x40, 160));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(px+ts/2-4, py+ts/2-5, 9, 9);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawPuzzle(Graphics2D g2, int px, int py, int col, int row) {
        int ts = tileSize;

        g2.setColor(new Color(0x05, 0x05, 0x18));
        g2.fillRect(px, py, ts, ts);

        float pulse = 0.5f + 0.5f*(float)Math.sin(ambientPhase + col*0.5f + row*0.7f);
        GradientPaint glow = new GradientPaint(
                px+ts/2f, py,
                new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), (int)(40+60*pulse)),
                px+ts/2f, py+ts,
                new Color(0, 0, 0, 0));
        g2.setPaint(glow);
        g2.fillRect(px, py, ts, ts);

        g2.setStroke(new BasicStroke(0.8f));
        float scanOffset = (ambientPhase * 16f) % ts;

        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                accentColor.getBlue(), (int)(80*pulse)));
        g2.drawLine(px, (int)(py + scanOffset), px+ts, (int)(py + scanOffset));

        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                accentColor.getBlue(), 40));
        for (int gx = px+8; gx < px+ts; gx += 10)
            for (int gy = py+8; gy < py+ts; gy += 10)
                g2.fillRect(gx, gy, 1, 1);
        g2.setStroke(new BasicStroke(1f));

        Graphics2D gr = (Graphics2D) g2.create();
        gr.translate(px + ts/2, py + ts/2);
        gr.rotate(ambientPhase * 0.5f);
        gr.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                accentColor.getBlue(), (int)(120 + 80*pulse)));
        gr.setStroke(new BasicStroke(1.5f));
        int hexR = 14;
        int[] hx = new int[6], hy = new int[6];
        for (int i = 0; i < 6; i++) {
            hx[i] = (int)(hexR * Math.cos(Math.PI/3 * i));
            hy[i] = (int)(hexR * Math.sin(Math.PI/3 * i));
        }
        gr.drawPolygon(hx, hy, 6);
        gr.rotate(-ambientPhase);
        gr.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                accentColor.getBlue(), (int)(80 + 60*pulse)));
        gr.drawPolygon(hx, hy, 6);
        gr.dispose();

        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(255,255,255, (int)(100 + 80*pulse)));
        g2.drawString("?", px+ts/2-5, py+ts/2+6);

        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                accentColor.getBlue(), (int)(100 + 80*pulse)));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(px+1, py+1, ts-2, ts-2);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawExit(Graphics2D g2, int px, int py) {
        int ts = tileSize;
        float p = 0.5f + 0.5f*(float)Math.sin(ambientPhase * 2);

        Color portalColor = exitUnlocked
                ? new Color(0x00, 0xE6, 0x76)
                : new Color(0xD5, 0x00, 0x00);
        Color portalGlow  = exitUnlocked
                ? new Color(0, 255, 120, (int)(80 + 60*p))
                : new Color(255, 30,  30, (int)(80 + 60*p));

        g2.setColor(new Color(0x03, 0x06, 0x0A));
        g2.fillRect(px, py, ts, ts);

        GradientPaint portalFill = new GradientPaint(
                px+ts/2f, py,       portalGlow,
                px+ts/2f, py+ts,    new Color(0,0,0,0));
        g2.setPaint(portalFill);
        g2.fillRect(px, py, ts, ts);

        Graphics2D gr = (Graphics2D) g2.create();
        gr.translate(px + ts/2, py + ts/2);
        gr.rotate(ambientPhase * (exitUnlocked ? 1.2f : -0.8f));
        gr.setColor(new Color(portalColor.getRed(), portalColor.getGreen(),
                portalColor.getBlue(), (int)(160 + 80*p)));
        gr.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gr.drawArc(-20, -20, 40, 40, 0, 270);
        gr.setStroke(new BasicStroke(1.5f));
        gr.drawArc(-16, -16, 32, 32, 90, 240);
        gr.dispose();

        RadialGradientPaint core = new RadialGradientPaint(
                px + ts/2f, py + ts/2f, ts/3f,
                new float[]{0f, 1f},
                new Color[]{
                        new Color(portalColor.getRed(), portalColor.getGreen(),
                                portalColor.getBlue(), (int)(180*p)),
                        new Color(0,0,0,0)
                });
        g2.setPaint(core);
        g2.fillOval(px+ts/2-18, py+ts/2-18, 36, 36);

        for (int i = 0; i < 8; i++) {
            float ang = (float)(Math.PI*2*i/8 + ambientPhase*1.5f);
            float r   = 18 + 4*(float)Math.sin(ambientPhase*3 + i);
            int   dpx = (int)(px + ts/2 + Math.cos(ang)*r);
            int   dpy = (int)(py + ts/2 + Math.sin(ang)*r);
            float da  = 0.4f + 0.6f*(float)Math.abs(Math.sin(ambientPhase*2+i));
            g2.setColor(new Color(portalColor.getRed(), portalColor.getGreen(),
                    portalColor.getBlue(), (int)(da*200)));
            g2.fillOval(dpx-2, dpy-2, 4, 4);
        }

        String label = exitUnlocked ? "EXIT" : "LOCK";
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(255,255,255, (int)(160 + 80*p)));
        g2.drawString(label, px + ts/2 - fm.stringWidth(label)/2, py+ts-6);

        g2.setColor(new Color(portalColor.getRed(), portalColor.getGreen(),
                portalColor.getBlue(), (int)(120+80*p)));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(px+1, py+1, ts-2, ts-2);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawAmbientBloom(Graphics2D g2, int offX, int offY) {

        Composite old = g2.getComposite();
        for (int r = 0; r < gridH; r++) {
            for (int c = 0; c < gridW; c++) {
                if (tiles[r][c] != TILE_PUZZLE && tiles[r][c] != TILE_EXIT) continue;
                int cx  = offX + c * tileSize + tileSize / 2;
                int cy  = offY + r * tileSize + tileSize / 2;
                float p = 0.4f + 0.3f * (float) Math.sin(ambientPhase * 1.5f + c + r);
                int   rad = (int)(tileSize * 1.8f);
                Color bc  = tiles[r][c] == TILE_EXIT && exitUnlocked
                        ? new Color(0, 230, 118, (int)(20 * p))
                        : new Color(accentColor.getRed(), accentColor.getGreen(),
                                    accentColor.getBlue(), (int)(16 * p));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2.setColor(bc);
                g2.fillOval(cx - rad, cy - rad, rad * 2, rad * 2);
            }
        }
        g2.setComposite(old);
    }

    private boolean isNearSpecial(int col, int row) {
        for (int dr = -2; dr <= 2; dr++)
            for (int dc = -2; dc <= 2; dc++) {
                int t = getTile(col+dc, row+dr);
                if (t == TILE_PUZZLE || t == TILE_EXIT) return true;
            }
        return false;
    }

    private float nearSpecialDist(int col, int row) {
        float minDist = 99f;
        for (int r = 0; r < gridH; r++)
            for (int c = 0; c < gridW; c++)
                if (tiles[r][c] == TILE_PUZZLE || tiles[r][c] == TILE_EXIT) {
                    float d = (float)Math.sqrt((col-c)*(col-c)+(row-r)*(row-r));
                    minDist = Math.min(minDist, d);
                }
        return minDist;
    }

    public void addItem(Item item)               { items.add(item); }
    public void addInteractable(Interactable ia) { interactables.add(ia); }

    public Item getItemAt(int col, int row) {
        return items.stream().filter(i -> !i.isCollected()&&i.getGridX()==col&&i.getGridY()==row)
                .findFirst().orElse(null);
    }
    public Interactable getInteractableAt(int col, int row) {
        return interactables.stream().filter(ia -> ia.getGridX()==col&&ia.getGridY()==row)
                .findFirst().orElse(null);
    }

    public void unlockExit()        { exitUnlocked = true; }
    public boolean isExitUnlocked() { return exitUnlocked; }

    public void reset() {
        exitUnlocked = false;
        items.forEach(Item::reset);
        interactables.forEach(Interactable::reset);
    }

    public void setTheme(Color floor, Color wall, Color accent) {
        this.floorColor = floor; this.wallColor = wall; this.accentColor = accent;
    }

    public String  getId()               { return id; }
    public String  getName()             { return name; }
    public int     getGridW()            { return gridW; }
    public int     getGridH()            { return gridH; }
    public int     getTileSize()         { return tileSize; }
    public List<Item> getItems()         { return items; }
    public List<Interactable> getInteractables() { return interactables; }
    public Color   getAccentColor()      { return accentColor; }
}
