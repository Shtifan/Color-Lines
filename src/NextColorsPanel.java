import javax.swing.*;
import java.awt.*;

public class NextColorsPanel extends JPanel {
    private BallButton[] nextColorButtons;
    private GameBoard gameBoard;

    public NextColorsPanel(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        setLayout(new FlowLayout());
        nextColorButtons = new BallButton[3];

        for (int i = 0; i < nextColorButtons.length; i++) {
            nextColorButtons[i] = new BallButton();
            nextColorButtons[i].setPreferredSize(new Dimension(50, 50));
            nextColorButtons[i].setEnabled(false);
            add(nextColorButtons[i]);
        }

        gameBoard.setNextColorsPanel(this);
    }

    public void updateNextColorsPreview() {
        Color[] nextColors = gameBoard.getColorManager().getNextColors();
        for (int i = 0; i < nextColors.length; i++) {
            nextColorButtons[i].setBallColor(nextColors[i]);
        }
    }
}