package game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameWindow extends JFrame {

    public static final int    WIDTH  = 1100;
    public static final int    HEIGHT = 720;
    public static final String TITLE  = "Time Loop Escape";

    private JPanel     container;
    private CardLayout cardLayout;
    private JPanel     currentScreen;
    private int        screenCounter = 0;

    public GameWindow() {
        super(TITLE);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setResizable(true);

        getContentPane().setBackground(Color.BLACK);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        GameWindow.this,
                        "Exit Time Loop Escape?",
                        "Quit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (choice == JOptionPane.YES_OPTION) {
                    game.core.GameEngine.getInstance().stop();
                }
            }
        });

        cardLayout = new CardLayout();
        container  = new JPanel(cardLayout);
        container.setBackground(Color.BLACK);
        container.setOpaque(true);
        add(container, BorderLayout.CENTER);
    }

    public void showScreen(JPanel screen) {
        String key = "screen_" + (screenCounter++);

        if (currentScreen != null) {
            container.remove(currentScreen);
        }

        container.add(screen, key);
        cardLayout.show(container, key);
        currentScreen = screen;

        container.revalidate();
        container.repaint();

        SwingUtilities.invokeLater(() -> {
            screen.revalidate();
            screen.repaint();
            screen.requestFocusInWindow();
        });

        System.out.println("[GameWindow] Showing: " + screen.getClass().getSimpleName());
    }

    public int getGameWidth()  { return getContentPane().getWidth(); }
    public int getGameHeight() { return getContentPane().getHeight(); }
}
