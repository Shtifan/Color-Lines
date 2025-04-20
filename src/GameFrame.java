import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameFrame extends JFrame {
    private GameBoard gameBoard;
    private ScoreManager scoreManager;
    private NextColorsPanel nextColorsPanel;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;

    public GameFrame() {
        setTitle("Connect 5 - Color Lines Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        scoreManager = new ScoreManager();
        gameBoard = new GameBoard(scoreManager);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameBoard.saveState();
            }
        });

        initializeTopPanel();
        initializeGamePanel();

        nextColorsPanel = new NextColorsPanel(gameBoard);
        add(nextColorsPanel, BorderLayout.SOUTH);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        if (!gameBoard.loadState()) {
            gameBoard.startNewGame();
        }
    }

    private void initializeTopPanel() {
        highScoreLabel = new JLabel("High Score: " + scoreManager.getHighScore());
        scoreLabel = new JLabel("Score: 0");
        scoreManager.setScoreLabels(scoreLabel, highScoreLabel);

        Font biggerFont = new Font("Arial", Font.BOLD, 18);
        highScoreLabel.setFont(biggerFont);
        scoreLabel.setFont(biggerFont);

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Start a new game? Current progress will be lost.", "New Game", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                gameBoard.startNewGame();
            }
        });

        newGameButton.setFocusPainted(false);
        newGameButton.setFont(new Font("Arial", Font.BOLD, 18));

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

    private void initializeGamePanel() {
        JPanel gamePanelContainer = new JPanel(new GridBagLayout());
        JPanel gamePanel = gameBoard.getPanel();
        gamePanel.setPreferredSize(new Dimension(600, 600));
        gamePanelContainer.add(gamePanel);

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
}