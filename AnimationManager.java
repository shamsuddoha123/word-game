import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AnimationManager {
    public Timer tileAnimationTimer;
    public Timer bubbleAnimationTimer;
    public List<Bubble> bubbles = new ArrayList<>();
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final Color TILE_COLOR = new Color(241, 196, 15);

    public void initializeBubbles(JPanel mainPanel) {
        // Create bubbles for background animation
        for (int i = 0; i < 30; i++) {
            bubbles.add(new Bubble(WINDOW_WIDTH, WINDOW_HEIGHT));
        }

        // Start bubble animation
        bubbleAnimationTimer = new Timer();
        bubbleAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Bubble bubble : bubbles) {
                    bubble.move(WINDOW_WIDTH, WINDOW_HEIGHT);
                }

                if (mainPanel != null) {
                    mainPanel.repaint();
                }
            }
        }, 0, 50);
    }

    public void startTileAnimation(JPanel tilesPanel) {
        if (tileAnimationTimer != null) {
            tileAnimationTimer.cancel();
        }

        tileAnimationTimer = new Timer();
        tileAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            public int direction = 1;
            public int count = 0;

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    Component[] components = tilesPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        if (components[i] instanceof JLabel) {
                            JLabel tile = (JLabel) components[i];

                            // Create a slight bouncing effect
                            int offset = (int)(Math.sin((count + i * 2) * 0.2) * 3);
                            tile.setBorder(BorderFactory.createCompoundBorder(
                                    new SoftBevelBorder(SoftBevelBorder.RAISED),
                                    BorderFactory.createEmptyBorder(5 + offset, 5, 5 - offset, 5)
                            ));
                        }
                    }
                    count++;
                });
            }
        }, 0, 50);
    }

    public void animateGridCell(JLabel cell) {
        Timer animTimer = new Timer();
        animTimer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                if (count < 5) {
                    SwingUtilities.invokeLater(() -> {
                        float[] hsb = Color.RGBtoHSB(TILE_COLOR.getRed(), TILE_COLOR.getGreen(), TILE_COLOR.getBlue(), null);
                        float brightness = hsb[2] + (count % 2 == 0 ? 0.1f : -0.1f);
                        brightness = Math.max(0.7f, Math.min(1.0f, brightness));
                        cell.setBackground(Color.getHSBColor(hsb[0], hsb[1], brightness));
                    });
                    count++;
                } else {
                    SwingUtilities.invokeLater(() -> {
                        cell.setBackground(TILE_COLOR);
                    });
                    animTimer.cancel();
                }
            }
        }, 0, 50);
    }

    public void startShuffleAnimation(JPanel letterTilesPanel) {
        Timer shuffleTimer = new Timer();
        shuffleTimer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                if (count < 5) {
                    SwingUtilities.invokeLater(() -> {
                        Component[] components = letterTilesPanel.getComponents();
                        for (Component component : components) {
                            if (component instanceof JLabel) {
                                JLabel tile = (JLabel) component;
                                int offset = (count % 2 == 0) ? 2 : -2;
                                tile.setLocation(tile.getX() + offset, tile.getY());
                            }
                        }
                    });
                    count++;
                } else {
                    shuffleTimer.cancel();
                }
            }
        }, 0, 50);
    }

    public void drawBubbles(Graphics2D g2d) {
        for (Bubble bubble : bubbles) {
            bubble.draw(g2d);
        }
    }
}
