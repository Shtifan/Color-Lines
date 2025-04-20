import javax.swing.*;
import java.io.*;

public class ScoreManager {
    private static final String HIGH_SCORE_FILE = System.getProperty("user.dir") + File.separator + "highscore.dat";
    private int score = 0;
    private int highScore = 0;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;

    public ScoreManager() {
        loadHighScore();
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
        }
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
        if (highScoreLabel != null) {
            highScoreLabel.setText("High Score: " + highScore);
        }
    }

    private void loadHighScore() {
        File file = new File(HIGH_SCORE_FILE);
        if (!file.exists() || file.length() == 0) {
            highScore = 0;
            return;
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            highScore = in.readInt();
        } catch (IOException e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        File file = new File(HIGH_SCORE_FILE);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            out.writeInt(highScore);
            out.flush();
        } catch (IOException e) {

        }
    }

    public void saveHighScoreNow() {
        saveHighScore();
    }
}