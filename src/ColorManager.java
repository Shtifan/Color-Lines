import java.awt.Color;
import java.util.Random;

public class ColorManager {
    private static final int COLORS = 7;
    private static final int NEXT_COLORS_COUNT = 3;

    private Color[] tileColors;
    private Color[] nextColors;
    private NextColorsPanel nextColorsPanel;
    private Random random;

    public ColorManager() {
        tileColors = new Color[] {
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                Color.MAGENTA, Color.CYAN, new Color(139, 69, 19) // Brown
        };
        nextColors = new Color[NEXT_COLORS_COUNT];
        random = new Random();
    }

    public void setNextColorsPanel(NextColorsPanel panel) {
        this.nextColorsPanel = panel;
    }

    public Color[] getNextColors() {
        return nextColors;
    }

    // Method to set next colors from loaded data
    public void setNextColors(Color[] colors) {
        if (colors != null && colors.length == nextColors.length) {
            for (int i = 0; i < colors.length; i++) {
                nextColors[i] = colors[i];
            }
            if (nextColorsPanel != null) {
                nextColorsPanel.updateNextColorsPreview();
            }
        }
    }

    public void generateNextColors() {
        for (int i = 0; i < nextColors.length; i++) {
            nextColors[i] = tileColors[random.nextInt(COLORS)];
        }
        if (nextColorsPanel != null) {
            nextColorsPanel.updateNextColorsPreview();
        }
    }
}