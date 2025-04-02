import javax.swing.*;
import java.io.*;

public class ScoreManager {
    private static final String DATA_FILE = System.getProperty("user.dir") + File.separator + "data.dat";
    private int score = 0;
    private int highScore = 0;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;

    public ScoreManager() {
        loadHighScore();
        System.out.println("ScoreManager initialized with high score: " + highScore);
        System.out.println("Using data file: " + DATA_FILE);
    }

    public void setScoreLabels(JLabel scoreLabel, JLabel highScoreLabel) {
        this.scoreLabel = scoreLabel;
        this.highScoreLabel = highScoreLabel;

        if (this.scoreLabel != null) {
            this.scoreLabel.setText("Score: " + score);
        }
        if (this.highScoreLabel != null) {
            this.highScoreLabel.setText("High Score: " + highScore);
        }
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setScore(int score) {
        this.score = score;
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }

    public void resetScore() {
        score = 0;
        if (scoreLabel != null) {
            scoreLabel.setText("Score: 0");
        }
    }

    public void addScore(int points) {
        score += points;
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }

    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
            if (highScoreLabel != null) {
                highScoreLabel.setText("High Score: " + highScore);
            }
            saveHighScore();

            System.out.println("New high score achieved and saved: " + highScore);
        }
    }

    private void loadHighScore() {
        File file = new File(DATA_FILE);
        if (!file.exists() || file.length() == 0) {
            System.out.println("High score file not found or empty: " + file.getAbsolutePath());
            highScore = 0;
            return;
        }

        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            highScore = in.readInt();
            System.out.println("High score loaded: " + highScore + " from " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error loading high score: " + e.getMessage());
            e.printStackTrace();
            highScore = 0;
        }
    }

    private void saveHighScore() {
        File file = new File(DATA_FILE);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
            System.out.println("Created directory: " + parent.getAbsolutePath());
        }

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            out.writeInt(highScore);
            out.flush();
            System.out.println("High score saved: " + highScore + " to " + file.getAbsolutePath());

            if (file.exists()) {
                System.out.println("Verified file exists with size: " + file.length() + " bytes");
            } else {
                System.err.println("WARNING: File does not exist after save attempt!");
            }
        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveHighScoreNow() {
        System.out.println("Forcing save of high score: " + highScore);
        saveHighScore();
    }
}