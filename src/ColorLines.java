import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class ColorLines extends JFrame {
    private static final int SIZE = 9;
    private static final int COLORS = 7;
    private static final int CONNECT_COUNT = 5;
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private BallButton[][] board;
    private Color[] tileColors;
    private Color[] nextColors;
    private int selectedRow = -1, selectedCol = -1;
    private int score = 0;
    private int highScore = 0;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;
    private BallButton[] nextColorButtons;
    private Random random;

    private static class BallButton extends JButton {
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

    public ColorLines() {
        setTitle("Connect 5 - Color Lines Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        if (board[row][col] != null) {
                            board[row][col].setSelected(false);
                        }
                    }
                }
            }
        });

        loadHighScore();

        initializeBoard();
        initializeColors();
        initializeTopPanel();
        initializeGamePanel();
        initializeNextColorsPanel();

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void initializeTopPanel() {
        highScoreLabel = new JLabel("High Score: " + highScore);
        scoreLabel = new JLabel("Score: 0");

        Font biggerFont = new Font("Arial", Font.BOLD, 18);
        highScoreLabel.setFont(biggerFont);
        scoreLabel.setFont(biggerFont);

        JButton newGameButton = getJButton();

        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);

        topPanel.add(highScoreLabel, gbc);

        gbc.gridy = 1;
        topPanel.add(scoreLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        topPanel.add(newGameButton, gbc);

        add(topPanel, BorderLayout.NORTH);
    }


    private JButton getJButton() {
        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Start a new game? Current progress will be lost.", "New Game", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                resetGame();
            }
        });

        newGameButton.setFocusPainted(false);
        newGameButton.setFont(new Font("Arial", Font.BOLD, 18));
        return newGameButton;
    }

    private void initializeBoard() {
        board = new BallButton[SIZE][SIZE];
        random = new Random();
    }

    private void initializeColors() {
        tileColors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, new Color(139, 69, 19) // Brown
        };
        nextColors = new Color[3];
        nextColorButtons = new BallButton[3];
    }

    private void initializeGamePanel() {
        JPanel gamePanelContainer = new JPanel(new GridBagLayout());
        JPanel gamePanel = new JPanel(new GridLayout(SIZE, SIZE));
        gamePanel.setPreferredSize(new Dimension(600, 600));
        gamePanelContainer.add(gamePanel);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                BallButton cell = new BallButton();
                cell.addActionListener(new CellClickListener(row, col));
                board[row][col] = cell;
                gamePanel.add(cell);
            }
        }

        gamePanelContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int size = Math.min(gamePanelContainer.getWidth(), gamePanelContainer.getHeight());
                gamePanel.setPreferredSize(new Dimension(size, size));
                gamePanelContainer.revalidate();
            }
        });

        add(gamePanelContainer, BorderLayout.CENTER);
    }

    private void initializeNextColorsPanel() {
        JPanel nextColorsPanel = new JPanel(new FlowLayout());
        for (int i = 0; i < nextColorButtons.length; i++) {
            nextColorButtons[i] = new BallButton();
            nextColorButtons[i].setPreferredSize(new Dimension(50, 50));
            nextColorButtons[i].setEnabled(false);
            nextColorsPanel.add(nextColorButtons[i]);
        }
        add(nextColorsPanel, BorderLayout.SOUTH);

        generateNextColors();
        updateNextColorsPreview();
        placeRandomTiles();
    }

    private void generateNextColors() {
        for (int i = 0; i < nextColors.length; i++) {
            nextColors[i] = tileColors[random.nextInt(COLORS)];
        }
    }

    private void updateNextColorsPreview() {
        for (int i = 0; i < nextColors.length; i++) {
            nextColorButtons[i].setBallColor(nextColors[i]);
        }
    }

    private void resetGame() {
        score = 0;
        scoreLabel.setText("Score: 0");
        selectedRow = -1;
        selectedCol = -1;

        // Clear the board
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col].setBallColor(null);
                board[row][col].setSelected(false);
            }
        }

        generateNextColors();
        updateNextColorsPreview();
        placeRandomTiles();
    }

    private void showGameOver(String message) {
        int choice = JOptionPane.showOptionDialog(this, message + "\nFinal Score: " + score, "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Play Again", "Exit"}, "Play Again");

        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            dispose();
            System.exit(0);
        }
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

        generateNextColors();
        updateNextColorsPreview();

        if (checkForConnects()) {
            if (score > highScore) {
                highScore = score;
                highScoreLabel.setText("High Score: " + highScore);
                saveHighScore();
            }
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
        queue.add(new int[]{fromRow, fromCol});
        visited[fromRow][fromCol] = true;

        int[] rowDir = {-1, 1, 0, 0};
        int[] colDir = {0, 0, -1, 1};

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

                if (isValidCell(newRow, newCol) && !visited[newRow][newCol] && board[newRow][newCol].getBallColor() == null) {
                    visited[newRow][newCol] = true;
                    queue.add(new int[]{newRow, newCol});
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

                    if (row <= SIZE - CONNECT_COUNT && col <= SIZE - CONNECT_COUNT && checkDirection(row, col, 1, 1, color)) {
                        markForClear(row, col, 1, 1, toClear);
                    }

                    if (row >= CONNECT_COUNT - 1 && col <= SIZE - CONNECT_COUNT && checkDirection(row, col, -1, 1, color)) {
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
                if (toClear[row][col] && toClear[row - 1][col] && toClear[row + 1][col] && toClear[row][col - 1] && toClear[row][col + 1]) {

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

            score += regularScore + bonusScore;
            scoreLabel.setText("Score: " + score);

            if (score > highScore) {
                highScore = score;
                highScoreLabel.setText("High Score: " + highScore);
                saveHighScore();
            }
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

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorLines::new);
    }
}