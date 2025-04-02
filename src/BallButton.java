import javax.swing.*;
import java.awt.*;

public class BallButton extends JButton {
    private Color ballColor;
    private boolean isSelected;
    private final Timer bounceTimer;
    private int bounceOffset = 0;
    private boolean bounceUp = false;
    private static final int MAX_BOUNCE_HEIGHT = 3;
    private static final int BOUNCE_STEP = 1;
    private static final int BOUNCE_DELAY = 25;

    public BallButton() {
        setContentAreaFilled(false);
        setBorderPainted(true);
        setBackground(Color.WHITE);
        ballColor = null;

        bounceTimer = new Timer(BOUNCE_DELAY, e -> {
            if (isSelected) {
                if (bounceUp) {
                    bounceOffset += BOUNCE_STEP;
                    if (bounceOffset >= MAX_BOUNCE_HEIGHT) {
                        bounceOffset = MAX_BOUNCE_HEIGHT;
                        bounceUp = false;
                    }
                } else {
                    bounceOffset -= BOUNCE_STEP;
                    if (bounceOffset <= 0) {
                        bounceOffset = 0;
                        bounceUp = true;
                    }
                }
                repaint();
            }
        });
    }

    public void setBallColor(Color color) {
        this.ballColor = color;
        repaint();
    }

    public Color getBallColor() {
        return ballColor;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        if (selected) {
            bounceOffset = 0;
            bounceUp = true;
            bounceTimer.start();
        } else {
            bounceTimer.stop();
            bounceOffset = 0;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (ballColor != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 10;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            if (isSelected) {
                y -= bounceOffset;
            }

            g2d.setColor(ballColor);
            g2d.fillOval(x, y, size, size);

            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(x + size / 4, y + size / 4, size / 4, size / 4);

            if (isSelected) {
                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(x, y, size, size);
            }

            g2d.dispose();
        }
    }
}