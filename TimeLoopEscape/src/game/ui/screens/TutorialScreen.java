package game.ui.screens;

import game.ui.BaseScreen;
import game.ui.GameWindow;

import javax.swing.*;
import java.awt.*;

public class TutorialScreen extends BaseScreen {

    public TutorialScreen(GameWindow window) {
        super(window);
    }

    @Override
    protected void init() {
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        topBar.setOpaque(false);
        JButton backBtn = createButton("← BACK", TEXT_DIM, () -> engine.showMainMenu());
        topBar.add(backBtn);
        add(topBar, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        centerPanel.add(Box.createVerticalStrut(60));

        JPanel contentCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x0D, 0x15, 0x20, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(0x26, 0x32, 0x38, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
            }
        };
        contentCard.setOpaque(false);
        contentCard.setLayout(new BoxLayout(contentCard, BoxLayout.Y_AXIS));
        contentCard.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        contentCard.setMaximumSize(new Dimension(600, 450));

        JLabel titleLbl = new JLabel("HOW TO PLAY");
        titleLbl.setFont(new Font("Monospaced", Font.BOLD, 28));
        titleLbl.setForeground(ACCENT_CYAN);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentCard.add(titleLbl);
        contentCard.add(Box.createVerticalStrut(20));

        String[] instructions = {
            "MOVEMENT:",
            "  • Use W, A, S, D to move your character.",
            "  • Hold SHIFT to sprint. Sprinting drains your sanity faster!",
            "",
            "OBJECTIVES:",
            "  • Search the facility for CLUES.",
            "  • Finding enough clues unlocks the escape door.",
            "  • The number of clues needed depends on the difficulty.",
            "",
            "TIME LOOP:",
            "  • You have a limited amount of time before the loop resets.",
            "  • Every reset deducts from your final score.",
            "  • Escape before you run out of loops!",
            "",
            "SANITY & VISION:",
            "  • Your sanity acts as your flashlight radius.",
            "  • As your sanity drops, the darkness closes in."
        };

        for (String line : instructions) {
            JLabel lbl = new JLabel(line);
            lbl.setFont(new Font("Monospaced", line.endsWith(":") ? Font.BOLD : Font.PLAIN, 14));
            lbl.setForeground(line.endsWith(":") ? ACCENT_AMBER : TEXT_PRIMARY);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentCard.add(lbl);
            contentCard.add(Box.createVerticalStrut(5));
        }

        JPanel cardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cardWrapper.setOpaque(false);
        cardWrapper.add(contentCard);

        centerPanel.add(cardWrapper);
        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    protected void onResize(int w, int h) {}

    @Override
    protected void drawScreen(Graphics2D g2, int w, int h) {
        // Background is already handled by BaseScreen
    }
}
