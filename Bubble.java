import java.awt.*;

public class Bubble {
    public int x, y;
    public int size;
    public float speed;
    public Color color;

    public Bubble(int windowWidth, int windowHeight) {
        x = (int)(Math.random() * windowWidth);
        y = windowHeight + (int)(Math.random() * 100);
        size = 10 + (int)(Math.random() * 30);
        speed = 0.5f + (float)(Math.random() * 1.5f);

        // Create a semi-transparent purple color
        int alpha = 50 + (int)(Math.random() * 100);
        color = new Color(150, 100, 200, alpha);
    }

    public void move(int windowWidth, int windowHeight) {
        y -= speed;
        if (y < -size) {
            y = windowHeight + size;
            x = (int)(Math.random() * windowWidth);
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval(x, y, size, size);
    }
}
