import java.io.Serializable;

public class GameState implements Serializable {
    public static final int SIZE = 9;
    public int[][] boardColors;
    public int[] nextColors;
    public int score;
    public int highScore;

    public GameState() {
        boardColors = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                boardColors[i][j] = -1;
        nextColors = new int[3];
        score = 0;
        highScore = 0;
    }
}
