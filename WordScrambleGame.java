import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WordScrambleGame extends JFrame {
    // Constants
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final String GAME_TITLE = "Word Scramble";
    public static final Color BG_COLOR_1 = new Color(68, 36, 102); // Dark Purple
    public static final Color BG_COLOR_2 = new Color(88, 46, 122); // Medium Purple
    public static final Color GRID_COLOR = new Color(110, 80, 150); // Light Purple for grid
    public static final Color TILE_COLOR = new Color(241, 196, 15); // Yellow for tiles
    public static final Color BUTTON_COLOR = new Color(46, 204, 113); // Green for buttons
    public static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 48);
    public static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 20);
    public static final Font GAME_FONT = new Font("Arial", Font.BOLD, 16);
    public static final int MAX_ROUNDS = 3;
    public static final int PADDING = 20; // Standard padding

    // Core components
    public GameState gameState;
    public UIManager uiManager;
    public GameLogic gameLogic;
    public FileManager fileManager;
    public AnimationManager animationManager;
    public CategoryGame categoryGame;

    public WordScrambleGame() {
        setTitle(GAME_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // Initialize components
        gameState = new GameState();
        fileManager = new FileManager();
        animationManager = new AnimationManager();
        gameLogic = new GameLogic(fileManager, animationManager);
        uiManager = new UIManager(animationManager, this);

        // Initialize UI
        uiManager.initializeUI();
        setContentPane(uiManager.getMainPanel());

        // Load word sets
        gameLogic.loadWordSets();

        categoryGame = new CategoryGame(this);

        // Show start screen
        showStartScreen();
    }

    public void showStartScreen() {
        uiManager.showScreen("start");
    }

    public void startCategoryGame() {
        categoryGame.startGame();
        uiManager.showScreen("categoryGame");
    }

    public void startMainGame() {
        startGame();
    }

    public void startGame() {
        // This is now Level 2 - the original word scramble game
        // Initialize game state
        gameState.resetForNewGame();

        // Update UI
        uiManager.roundLabel.setText("Round: " + gameState.currentRound);
        uiManager.scoreLabel.setText("Score: " + gameState.score);
        updateTimeLabel();

        // Generate word set for the current round
        generateWordSet();

        // Start game timer
        startGameTimer();

        // Show game screen
        uiManager.showScreen("game");
    }

    public void startGameTimer() {
        if (gameState.gameTimer != null) {
            gameState.gameTimer.cancel();
        }

        gameState.gameTimer = new Timer();
        gameState.gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!gameState.isPaused) {
                    gameState.timeRemaining--;

                    // Update health bar based on 3 minutes (180 seconds) - FIXED
                    // Health bar has 4 sections, each representing 45 seconds
                    int sectionsRemaining = (int) Math.ceil(gameState.timeRemaining / 45.0);
                    gameState.healthPercentage = Math.max(0, Math.min(100, sectionsRemaining * 25));

                    SwingUtilities.invokeLater(() -> {
                        updateTimeLabel();

                        // Check if time is up
                        if (gameState.timeRemaining <= 0) {
                            gameState.gameTimer.cancel();
                            gameOver();
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    public void updateTimeLabel() {
        int minutes = gameState.timeRemaining / 60;
        int seconds = gameState.timeRemaining % 60;
        uiManager.timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    public void pauseGame() {
        gameState.isPaused = true;
        uiManager.showScreen("pause");
    }

    public void resumeGame() {
        gameState.isPaused = false;
        uiManager.showScreen("game");
    }

    public void showMenu() {
        gameState.isPaused = true;
        uiManager.showScreen("menu");
    }

    public void toggleSound(JButton soundButton) {
        gameState.isSoundOn = !gameState.isSoundOn;
        soundButton.setText("Sound " + (gameState.isSoundOn ? "ON" : "OFF"));
    }

    public void toggleMusic(JButton musicButton) {
        gameState.isMusicOn = !gameState.isMusicOn;
        musicButton.setText("Music " + (gameState.isMusicOn ? "ON" : "OFF"));
    }

    public void showWordsFound() {
        // FIXED: Recreate the words found screen with current data
        uiManager.createWordsFoundScreen();
        uiManager.showScreen("wordsFound");
    }

    public void showHelp() {
        JOptionPane.showMessageDialog(this,
                "Word Scramble Game Help:\n\n" +
                        "1. Use the 5 scrambled letters to form words.\n" +
                        "2. Click on letter tiles to select them.\n" +
                        "3. Selected letters appear in the word input box.\n" +
                        "4. Words are automatically detected and placed when valid.\n" +
                        "5. Find 3 three-letter words, 2 four-letter words, and 1 five-letter word.\n" +
                        "6. Use the shuffle button to rearrange the same letters.\n" +
                        "7. Use the hint button to reveal letters one by one.\n" +
                        "8. Complete all three rounds to win!",
                "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    public void quitGame() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to quit the game?",
                "Quit Game", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        } else {
            resumeGame();
        }
    }

    public void generateWordSet() {
        gameLogic.generateWordSet(gameState);

        // Update UI
        uiManager.updateLetterTiles();
        uiManager.clearAllGrids();
        clearCurrentWord();
    }

    public void clearCurrentWord() {
        gameState.currentInputWord = "";
        uiManager.updateWordInputDisplay();
    }

    public void shuffleLetters() {
        // Generate a completely new word set with new scrambled letters
        generateWordSet();

        // Reset game state (score, timer, hints) but keep the same round
        gameState.score = 0;
        gameState.timeRemaining = 180;
        gameState.hintsUsed = 0;
        gameState.healthPercentage = 100;
        gameState.wordsFound = new boolean[6]; // Reset found words
        gameState.currentGameState = "playing";

        // Update UI
        uiManager.scoreLabel.setText("Score: " + gameState.score);
        updateTimeLabel();
        clearCurrentWord();

        // Restart the timer
        startGameTimer();

        // Add a shuffle animation effect
        animationManager.startShuffleAnimation(uiManager.letterTilesPanel);
    }

    public void checkWordAutomatically() {
        boolean wordFound = gameLogic.checkWordAutomatically(gameState,
                uiManager.threeLetterLabels, uiManager.fourLetterLabels,
                uiManager.fiveLetterLabels, uiManager.scoreLabel, this);

        // FIXED: Only clear if word was found, and check for round completion
        if (wordFound) {
            // Clear the current word immediately
            clearCurrentWord();

            // Check if all words are found
            boolean allWordsFound = true;
            for (boolean found : gameState.wordsFound) {
                if (!found) {
                    allWordsFound = false;
                    break;
                }
            }

            if (allWordsFound) {
                roundComplete();
            }
        }
    }

    public void roundComplete() {
        // Stop the timer
        gameState.gameTimer.cancel();

        // Set the game state to round complete
        gameState.currentGameState = "roundComplete";

        // FIXED: Recreate the round complete screen with current data
        uiManager.createRoundCompleteScreen();

        // Show round complete screen
        uiManager.showScreen("roundComplete");
    }

    public void startNextRound() {
        // Increment round
        gameState.currentRound++;

        if (gameState.currentRound <= GameState.MAX_ROUNDS) {
            // Reset for new round
            gameState.resetForNewRound();

            // Update UI
            uiManager.roundLabel.setText("Round: " + gameState.currentRound);
            updateTimeLabel();

            // Generate new word set
            generateWordSet();

            // Start game timer
            startGameTimer();

            // Show game screen
            uiManager.showScreen("game");
        } else {
            // Game completed
            JOptionPane.showMessageDialog(this,
                    "Congratulations! You've completed all rounds!\nFinal Score: " + gameState.score,
                    "Game Complete", JOptionPane.INFORMATION_MESSAGE);

            // Restart the game
            restartGame();
        }
    }

    public void gameOver() {
        // Stop the timer
        if (gameState.gameTimer != null) {
            gameState.gameTimer.cancel();
        }

        // Set the game state to game over
        gameState.currentGameState = "gameOver";

        // FIXED: Recreate the game over screen with current data
        uiManager.createGameOverScreen();

        // Show game over screen
        uiManager.showScreen("gameOver");
    }

    public void restartGame() {
        // Stop any existing timer first
        if (gameState.gameTimer != null) {
            gameState.gameTimer.cancel();
        }

        // Reset game state
        gameState.resetForNewGame();

        // Start the game
        startGame();
    }

    public void showHint() {
        // Check if hints are exhausted
        if (gameState.hintsUsed >= 4) {
            return; // No popup, just return silently
        }

        // Find a word that hasn't been found yet
        List<Integer> remainingWordIndices = new ArrayList<>();
        for (int i = 0; i < gameState.wordsFound.length; i++) {
            if (!gameState.wordsFound[i]) {
                remainingWordIndices.add(i);
            }
        }

        if (!remainingWordIndices.isEmpty()) {
            // Get a random word from the remaining words
            int randomIndex = remainingWordIndices.get((int)(Math.random() * remainingWordIndices.size()));
            String hintWord = gameState.currentWordSet[randomIndex];

            // Reveal one letter in the appropriate grid
            gameLogic.revealLetterInGrid(hintWord, randomIndex, uiManager.threeLetterLabels,
                    uiManager.fourLetterLabels, uiManager.fiveLetterLabels);

            // Deduct 10 points and increment hint counter
            gameState.score -= 10;
            gameState.hintsUsed++;
            uiManager.scoreLabel.setText("Score: " + gameState.score);

            // Show auto-closing hint message
            uiManager.showAutoClosingMessage(
                    "Revealed a letter for: " + hintWord.length() + "-letter word (-10 points)",
                    "Hint Used!",
                    2000
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WordScrambleGame game = new WordScrambleGame();
            game.setVisible(true);
        });
    }
}
