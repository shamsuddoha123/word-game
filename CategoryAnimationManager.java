import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CategoryAnimationManager {
    public Timer animationTimer;
    public List<Bird> birds = new ArrayList<>();
    public List<Star> stars = new ArrayList<>();
    public List<Confetti> confettiList = new ArrayList<>();
    public boolean showConfetti = false;
    public List<Cloud> clouds = new ArrayList<>();

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    public void initializeAnimations(JPanel panel) {
        // Create twinkling stars
        for (int i = 0; i < 25; i++) {
            stars.add(new Star(WINDOW_WIDTH, WINDOW_HEIGHT));
        }

        // Create flying birds for background animation
        for (int i = 0; i < 8; i++) {
            birds.add(new Bird(WINDOW_WIDTH, WINDOW_HEIGHT));
        }

        // Create floating clouds
        for (int i = 0; i < 4; i++) {
            Cloud cloud = new Cloud(WINDOW_WIDTH, WINDOW_HEIGHT);
            cloud.x = (float)(Math.random() * WINDOW_WIDTH); // Spread initial positions
            clouds.add(cloud);
        }

        // Start animation timer
        animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateAnimations();
                if (panel != null) {
                    panel.repaint();
                }
            }
        }, 0, 50);
    }

    public void updateAnimations() {
        // Update stars
        for (Star star : stars) {
            star.update();
        }

        // Update birds
        for (Bird bird : birds) {
            bird.update(WINDOW_WIDTH, WINDOW_HEIGHT);
        }

        // Update clouds
        for (Cloud cloud : clouds) {
            cloud.update(WINDOW_WIDTH, WINDOW_HEIGHT);
        }

        // Update confetti
        if (showConfetti) {
            Iterator<Confetti> iterator = confettiList.iterator();
            while (iterator.hasNext()) {
                Confetti confetti = iterator.next();
                confetti.update();
                if (confetti.y > WINDOW_HEIGHT + 50) {
                    iterator.remove();
                }
            }

            // Stop confetti after a while
            if (confettiList.isEmpty()) {
                showConfetti = false;
            }
        }
    }

    public void drawAnimations(Graphics2D g2d) {
        // Draw clouds first (background layer)
        for (Cloud cloud : clouds) {
            cloud.draw(g2d);
        }

        // Draw stars
        for (Star star : stars) {
            star.draw(g2d);
        }

        // Draw birds
        for (Bird bird : birds) {
            bird.draw(g2d);
        }

        // Draw confetti
        if (showConfetti) {
            for (Confetti confetti : confettiList) {
                confetti.draw(g2d);
            }
        }
    }

    public void triggerConfetti() {
        showConfetti = true;
        confettiList.clear();

        // Create confetti burst
        for (int i = 0; i < 50; i++) {
            confettiList.add(new Confetti(WINDOW_WIDTH / 2, 100));
        }
    }

    public void stopAnimations() {
        if (animationTimer != null) {
            animationTimer.cancel();
        }
    }
}

class Bird {
    public float x, y;
    public float speedX, speedY;
    public float size;
    public Color color;
    public float wingFlap = 0;
    public float flapSpeed;
    public int direction; // 1 for right, -1 for left

    public Bird(int windowWidth, int windowHeight) {
        x = (float)(Math.random() * windowWidth);
        y = 50 + (float)(Math.random() * (windowHeight - 200)); // Keep birds in upper area
        speedX = 1 + (float)(Math.random() * 2); // 1-3 speed
        speedY = (float)(Math.random() * 0.5 - 0.25); // Slight vertical movement
        size = 15 + (float)(Math.random() * 10);
        flapSpeed = 0.2f + (float)(Math.random() * 0.1);
        direction = Math.random() > 0.5 ? 1 : -1;
        speedX *= direction;

        // Cute bird colors
        Color[] birdColors = {
                new Color(255, 100, 100, 180), // Red bird
                new Color(100, 150, 255, 180), // Blue bird
                new Color(255, 200, 100, 180), // Yellow bird
                new Color(150, 255, 150, 180), // Green bird
                new Color(255, 150, 255, 180), // Pink bird
                new Color(200, 100, 255, 180)  // Purple bird
        };
        color = birdColors[(int)(Math.random() * birdColors.length)];
    }

    public void update(int windowWidth, int windowHeight) {
        x += speedX;
        y += speedY;
        wingFlap += flapSpeed;

        // Reset bird when it goes off screen
        if (direction > 0 && x > windowWidth + 50) {
            x = -50;
            y = 50 + (float)(Math.random() * (windowHeight - 200));
        } else if (direction < 0 && x < -50) {
            x = windowWidth + 50;
            y = 50 + (float)(Math.random() * (windowHeight - 200));
        }

        // Keep birds in reasonable vertical bounds
        if (y < 30) speedY = Math.abs(speedY);
        if (y > windowHeight - 150) speedY = -Math.abs(speedY);
    }

    public void draw(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate((int)x, (int)y);
        if (direction < 0) {
            g2d.scale(-1, 1); // Flip for left-flying birds
        }

        g2d.setColor(color);

        // Draw bird body (oval)
        g2d.fillOval(-8, -4, 16, 8);

        // Draw wings with flapping animation
        int wingOffset = (int)(Math.sin(wingFlap) * 3);
        g2d.fillOval(-6, -8 + wingOffset, 12, 4); // Upper wing
        g2d.fillOval(-6, 4 - wingOffset, 12, 4);  // Lower wing

        // Draw beak
        g2d.setColor(new Color(255, 150, 0, 200)); // Orange beak
        int[] beakX = {8, 12, 8};
        int[] beakY = {-1, 0, 1};
        g2d.fillPolygon(beakX, beakY, 3);

        // Draw eye
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillOval(2, -2, 3, 3);

        g2d.setTransform(oldTransform);
    }
}

class Star {
    public float x, y;
    public float twinkle;
    public float twinkleSpeed;
    public Color color;
    public float size;

    public Star(int windowWidth, int windowHeight) {
        x = (float)(Math.random() * windowWidth);
        y = (float)(Math.random() * windowHeight);
        twinkle = (float)(Math.random() * Math.PI * 2);
        twinkleSpeed = (float)(Math.random() * 0.1 + 0.05);
        size = 3 + (float)(Math.random() * 4);

        // Softer star colors
        Color[] starColors = {
                new Color(255, 255, 255, 150),
                new Color(255, 255, 224, 150), // Light yellow
                new Color(255, 182, 193, 150), // Light pink
                new Color(173, 216, 230, 150)  // Light blue
        };
        color = starColors[(int)(Math.random() * starColors.length)];
    }

    public void update() {
        twinkle += twinkleSpeed;
    }

    public void draw(Graphics2D g2d) {
        float alpha = (float)(Math.sin(twinkle) * 0.5 + 0.5);
        Color twinkleColor = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int)(alpha * color.getAlpha()));
        g2d.setColor(twinkleColor);

        // Draw star shape
        drawStar(g2d, (int)x, (int)y, (int)size);
    }

    private void drawStar(Graphics2D g2d, int x, int y, int size) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * i / 5.0;
            int radius = (i % 2 == 0) ? size : size / 2;
            xPoints[i] = x + (int)(Math.cos(angle) * radius);
            yPoints[i] = y + (int)(Math.sin(angle) * radius);
        }

        g2d.fillPolygon(xPoints, yPoints, 10);
    }
}

class Confetti {
    public float x, y;
    public float speedX, speedY;
    public float gravity = 0.3f;
    public Color color;
    public float size;
    public float rotation = 0;
    public float rotationSpeed;

    public Confetti(int startX, int startY) {
        x = startX + (float)(Math.random() * 100 - 50);
        y = startY;
        speedX = (float)(Math.random() * 10 - 5);
        speedY = (float)(Math.random() * -10 - 5);
        size = 5 + (float)(Math.random() * 5);
        rotationSpeed = (float)(Math.random() * 0.3 - 0.15);

        // Softer confetti colors
        Color[] confettiColors = {
                new Color(255, 182, 193), // Light pink
                new Color(173, 216, 230), // Light blue
                new Color(255, 218, 185), // Peach
                new Color(221, 160, 221), // Plum
                new Color(152, 251, 152), // Pale green
                new Color(255, 239, 213)  // Papaya whip
        };
        color = confettiColors[(int)(Math.random() * confettiColors.length)];
    }

    public void update() {
        x += speedX;
        y += speedY;
        speedY += gravity;
        rotation += rotationSpeed;
    }

    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate((int)x, (int)y);
        g2d.rotate(rotation);

        g2d.setColor(color);
        int halfSize = (int)(size / 2);
        g2d.fillRect(-halfSize, -halfSize, (int)size, (int)size);

        g2d.setTransform(oldTransform);
    }
}

class Cloud {
    public float x, y;
    public float speedX;
    public float size;
    public Color color;
    public int cloudType; // Different cloud shapes

    public Cloud(int windowWidth, int windowHeight) {
        x = -150; // Start off-screen to the left
        y = 50 + (float)(Math.random() * (windowHeight - 200)); // Random height in upper area
        speedX = 0.3f + (float)(Math.random() * 0.7f); // Slow floating speed
        size = 0.8f + (float)(Math.random() * 0.4f); // Size variation
        cloudType = (int)(Math.random() * 3); // 3 different cloud shapes

        // White clouds with slight transparency
        color = new Color(255, 255, 255, 200 + (int)(Math.random() * 55)); // 200-255 alpha
    }

    public void update(int windowWidth, int windowHeight) {
        x += speedX;

        // Reset cloud when it goes off screen to the right
        if (x > windowWidth + 150) {
            x = -150;
            y = 50 + (float)(Math.random() * (windowHeight - 200));
            speedX = 0.3f + (float)(Math.random() * 0.7f);
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);

        int baseX = (int)x;
        int baseY = (int)y;
        int baseSize = (int)(60 * size);

        // Draw different cloud shapes based on cloudType
        switch (cloudType) {
            case 0: // Standard cloud
                g2d.fillOval(baseX, baseY, (int)(80 * size), (int)(50 * size));
                g2d.fillOval(baseX + (int)(30 * size), baseY - (int)(10 * size), (int)(60 * size), (int)(40 * size));
                g2d.fillOval(baseX + (int)(60 * size), baseY, (int)(70 * size), (int)(45 * size));
                break;
            case 1: // Fluffy cloud
                g2d.fillOval(baseX, baseY, (int)(90 * size), (int)(55 * size));
                g2d.fillOval(baseX + (int)(40 * size), baseY - (int)(15 * size), (int)(70 * size), (int)(45 * size));
                g2d.fillOval(baseX + (int)(70 * size), baseY, (int)(80 * size), (int)(50 * size));
                g2d.fillOval(baseX + (int)(20 * size), baseY + (int)(10 * size), (int)(50 * size), (int)(30 * size));
                break;
            case 2: // Stretched cloud
                g2d.fillOval(baseX, baseY, (int)(100 * size), (int)(60 * size));
                g2d.fillOval(baseX + (int)(50 * size), baseY - (int)(5 * size), (int)(80 * size), (int)(50 * size));
                g2d.fillOval(baseX + (int)(90 * size), baseY, (int)(90 * size), (int)(55 * size));
                break;
        }
    }
}
