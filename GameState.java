import java.util.*;

public class GameState {
    // Game state variables
    public int currentRound = 1;
    public int score = 0;
    public int timeRemaining = 180; // 5 minutes in seconds
    public boolean isPaused = false;
    public boolean isSoundOn = true;
    public boolean isMusicOn = true;
    public String[] currentWordSet = new String[6]; // 3 three-letter, 2 four-letter, 1 five-letter
    public boolean[] wordsFound = new boolean[6];
    public String scrambledLetters = "";
    public Timer gameTimer;
    public int healthPercentage = 100;
    public String currentInputWord = "";
    public int hintsUsed = 0;

    // NEW: Track the current game state
    public String currentGameState = "playing"; // "playing", "roundComplete", "gameOver"

    // Constants
    public static final int MAX_ROUNDS = 3;

    public void resetForNewGame() {
        currentRound = 1;
        score = 0;
        timeRemaining = 180;
        isPaused = false;
        healthPercentage = 100;
        hintsUsed = 0;
        currentInputWord = "";
        wordsFound = new boolean[6];
        currentGameState = "playing";
    }

    public void resetForNewRound() {
        timeRemaining = 180;
        healthPercentage = 100;
        hintsUsed = 0; // Reset hint counter for new round
        currentInputWord = "";
        wordsFound = new boolean[6];
        currentGameState = "playing";
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
