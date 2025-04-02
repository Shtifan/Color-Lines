import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class GameBoard {
    private static final int SIZE = 9;
    private static final int CONNECT_COUNT = 5;

    private BallButton[][] board;
    private int selectedRow = -1, selectedCol = -1;
    private ColorManager colorManager;
    private ScoreManager scoreManager;
    private JPanel gamePanel;
    private Random random;

    public GameBoard(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        board = new BallButton[SIZE][SIZE];
        random = new Random();
        colorManager = new ColorManager();
        initializeBoard();
    }

    // Getter for the board array
    public BallButton[][] getBoard() {
        return board;
    }

    // Getters for selected position
    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public void setNextColorsPanel(NextColorsPanel panel) {
        colorManager.setNextColorsPanel(panel);
    }

    public ColorManager getColorManager() {
        return colorManager;
    }

    private void initializeBoard() {
        gamePanel = new JPanel(new GridLayout(SIZE, SIZE));

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                BallButton cell = new BallButton();
                cell.addActionListener(new CellClickListener(row, col));
                board[row][col] = cell;
                gamePanel.add(cell);
            }
        }
    }

    public JPanel getPanel() {
        return gamePanel;
    }

    public void startNewGame() {
        scoreManager.resetScore();
        selectedRow = -1;
        selectedCol = -1;

        // Clear the board
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col].setBallColor(null);
                board[row][col].setSelected(false);
            }
        }

        colorManager.generateNextColors();
        placeRandomTiles();
    }

    private void placeRandomTiles() {
        int emptySpaces = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col].getBallColor() == null) {
                    emptySpaces++;
                }
            }
        }

        Color[] nextColors = colorManager.getNextColors();
        if (emptySpaces < nextColors.length) {
            showGameOver("Game Over! Not enough space to place new balls.");
            return;
        }

        for (Color color : nextColors) {
            int row, col;
            do {
                row = random.nextInt(SIZE);
                col = random.nextInt(SIZE);
            } while (board[row][col].getBallColor() != null);

            board[row][col].setBallColor(color);
        }

        colorManager.generateNextColors();

        if (checkForConnects()) {
            scoreManager.updateHighScore();
        }

        if (isBoardFull()) {
            showGameOver("Game Over! No more moves available.");
        }
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        return board[toRow][toCol].getBallColor() == null && hasPath(fromRow, fromCol, toRow, toCol);
    }

    private boolean hasPath(int fromRow, int fromCol, int toRow, int toCol) {
        boolean[][] visited = new boolean[SIZE][SIZE];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[] { fromRow, fromCol });
        visited[fromRow][fromCol] = true;

        int[] rowDir = { -1, 1, 0, 0 };
        int[] colDir = { 0, 0, -1, 1 };

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int currRow = current[0];
            int currCol = current[1];

            if (currRow == toRow && currCol == toCol) {
                return true;
            }

            for (int i = 0; i < 4; i++) {
                int newRow = currRow + rowDir[i];
                int newCol = currCol + colDir[i];

                if (isValidCell(newRow, newCol) && !visited[newRow][newCol] &&
                        board[newRow][newCol].getBallColor() == null) {
                    visited[newRow][newCol] = true;
                    queue.add(new int[] { newRow, newCol });
                }
            }
        }

        return false;
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    private boolean checkForConnects() {
        boolean[][] toClear = new boolean[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col].getBallColor() != null) {
                    Color color = board[row][col].getBallColor();

                    if (col <= SIZE - CONNECT_COUNT && checkDirection(row, col, 0, 1, color)) {
                        markForClear(row, col, 0, 1, toClear);
                    }

                    if (row <= SIZE - CONNECT_COUNT && checkDirection(row, col, 1, 0, color)) {
                        markForClear(row, col, 1, 0, toClear);
                    }

                    if (row <= SIZE - CONNECT_COUNT && col <= SIZE - CONNECT_COUNT &&
                            checkDirection(row, col, 1, 1, color)) {
                        markForClear(row, col, 1, 1, toClear);
                    }

                    if (row >= CONNECT_COUNT - 1 && col <= SIZE - CONNECT_COUNT &&
                            checkDirection(row, col, -1, 1, color)) {
                        markForClear(row, col, -1, 1, toClear);
                    }
                }
            }
        }

        boolean[][] crossPattern = detectCrossPatterns(toClear);

        return clearMarkedCells(toClear, crossPattern);
    }

    private boolean[][] detectCrossPatterns(boolean[][] toClear) {
        boolean[][] crossPattern = new boolean[SIZE][SIZE];

        for (int row = 1; row < SIZE - 1; row++) {
            for (int col = 1; col < SIZE - 1; col++) {
                if (toClear[row][col] && toClear[row - 1][col] && toClear[row + 1][col] &&
                        toClear[row][col - 1] && toClear[row][col + 1]) {

                    crossPattern[row][col] = true;
                    crossPattern[row - 1][col] = true;
                    crossPattern[row + 1][col] = true;
                    crossPattern[row][col - 1] = true;
                    crossPattern[row][col + 1] = true;
                }
            }
        }
        return crossPattern;
    }

    private boolean checkDirection(int row, int col, int dRow, int dCol, Color color) {
        for (int i = 0; i < CONNECT_COUNT; i++) {
            if (board[row + i * dRow][col + i * dCol].getBallColor() != color) {
                return false;
            }
        }
        return true;
    }

    private void markForClear(int row, int col, int dRow, int dCol, boolean[][] toClear) {
        for (int i = 0; i < CONNECT_COUNT; i++) {
            toClear[row + i * dRow][col + i * dCol] = true;
        }
    }

    private boolean clearMarkedCells(boolean[][] toClear, boolean[][] crossPattern) {
        int cleared = 0;
        int crossBonus = 0;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (toClear[row][col]) {
                    board[row][col].setBallColor(null);
                    cleared++;
                    if (crossPattern[row][col]) {
                        crossBonus++;
                    }
                }
            }
        }

        if (cleared > 0) {
            int regularScore = cleared * 2;
            int bonusScore = crossBonus * 8;
            scoreManager.addScore(regularScore + bonusScore);
            return true;
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col].getBallColor() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showGameOver(String message) {
        int choice = JOptionPane.showOptionDialog(null,
                message + "\nFinal Score: " + scoreManager.getScore(),
                "Game Over", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null,
                new String[] { "Play Again", "Exit" }, "Play Again");

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            System.exit(0);
        }
    }

    private class CellClickListener implements ActionListener {
        private final int row;
        private final int col;

        public CellClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedRow != -1 && selectedCol != -1) {
                board[selectedRow][selectedCol].setSelected(false);
            }

            Color ballColor = board[row][col].getBallColor();

            if (ballColor != null) {
                selectedRow = row;
                selectedCol = col;
                board[row][col].setSelected(true);
            } else if (selectedRow != -1 && selectedCol != -1) {
                if (isValidMove(selectedRow, selectedCol, row, col)) {
                    board[row][col].setBallColor(board[selectedRow][selectedCol].getBallColor());
                    board[selectedRow][selectedCol].setBallColor(null);
                    board[selectedRow][selectedCol].setSelected(false);

                    selectedRow = -1;
                    selectedCol = -1;

                    // Force refresh of the UI
                    gamePanel.revalidate();
                    gamePanel.repaint();

                    if (!checkForConnects()) {
                        placeRandomTiles();

                        if (isBoardFull()) {
                            showGameOver("Game Over! No more moves available.");
                        }
                    }
                }
            }
        }
    }

    public void deselectAll() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] != null) {
                    board[row][col].setSelected(false);
                }
            }
        }
    }
}