import javax.swing.*;
import java.util.*;
import java.util.List;
import java.awt.Color;

public class GameLogic {
    public List<WordSet> allWordSets = new ArrayList<>();
    public FileManager fileManager;
    public AnimationManager animationManager;

    public GameLogic(FileManager fileManager, AnimationManager animationManager) {
        this.fileManager = fileManager;
        this.animationManager = animationManager;
    }

    public void loadWordSets() {
        // Load word sets from file
        allWordSets = fileManager.loadWordSetsFromFile(FileManager.WORD_SETS_FILE);

        // If file doesn't exist or is empty, create default word sets
        if (allWordSets.isEmpty()) {
            allWordSets = fileManager.createDefaultWordSets();
            fileManager.saveWordSetsToFile(FileManager.WORD_SETS_FILE, allWordSets);
        }
    }

    public void generateWordSet(GameState gameState) {
        // Select a random word set from the loaded sets
        if (!allWordSets.isEmpty()) {
            WordSet selectedSet = allWordSets.get((int)(Math.random() * allWordSets.size()));
            gameState.currentWordSet = selectedSet.words.clone();
            gameState.scrambledLetters = shuffleString(selectedSet.letters);
        } else {
            // Fallback to default word set
            gameState.currentWordSet = new String[]{"HAM", "HAS", "ASH", "MASH", "MASS", "SMASH"};
            gameState.scrambledLetters = "SHSMA";
        }

        // Reset found words
        gameState.wordsFound = new boolean[6];
    }

    public String shuffleString(String input) {
        char[] characters = input.toCharArray();

        // Shuffle the array
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = (int)(Math.random() * characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }

        return new String(characters);
    }

    public boolean checkWordAutomatically(GameState gameState, JLabel[][] threeLetterLabels,
                                          JLabel[][] fourLetterLabels, JLabel[] fiveLetterLabels,
                                          JLabel scoreLabel, WordScrambleGame game) {
        if (gameState.currentInputWord.length() < 3) {
            return false; // Need at least 3 letters
        }

        // Check if the word matches any of the target words
        boolean wordFound = false;
        int wordIndex = -1;

        for (int i = 0; i < gameState.currentWordSet.length; i++) {
            if (gameState.currentWordSet[i].equalsIgnoreCase(gameState.currentInputWord) && !gameState.wordsFound[i]) {
                wordFound = true;
                wordIndex = i;
                break;
            }
        }

        if (wordFound) {
            gameState.wordsFound[wordIndex] = true;
            gameState.score += 30; // Fixed 30 points per word
            scoreLabel.setText("Score: " + gameState.score);

            // Place the word in the appropriate grid
            placeWordInGrid(gameState.currentInputWord, wordIndex, threeLetterLabels, fourLetterLabels, fiveLetterLabels);

            // Check how many words have been found
            int wordsFoundCount = 0;
            for (boolean found : gameState.wordsFound) {
                if (found) wordsFoundCount++;
            }

            // Auto-reveal a letter after 2 words found
            if (wordsFoundCount == 2) {
                autoRevealLetter(gameState, threeLetterLabels, fourLetterLabels, fiveLetterLabels, game);
            }

            // Check if all words are found
            boolean allWordsFound = true;
            for (boolean found : gameState.wordsFound) {
                if (!found) {
                    allWordsFound = false;
                    break;
                }
            }

            return allWordsFound;
        }
        return false;
    }

    public void placeWordInGrid(String word, int wordIndex, JLabel[][] threeLetterLabels,
                                JLabel[][] fourLetterLabels, JLabel[] fiveLetterLabels) {
        if (wordIndex < 3) {
            // 3-letter word
            int row = wordIndex;
            for (int j = 0; j < 3; j++) {
                if (j < word.length()) {
                    threeLetterLabels[row][j].setText(String.valueOf(word.charAt(j)));
                    threeLetterLabels[row][j].setOpaque(true);
                    threeLetterLabels[row][j].setBackground(new Color(241, 196, 15));
                    animationManager.animateGridCell(threeLetterLabels[row][j]);
                }
            }
        } else if (wordIndex < 5) {
            // 4-letter word
            int row = wordIndex - 3;
            for (int j = 0; j < 4; j++) {
                if (j < word.length()) {
                    fourLetterLabels[row][j].setText(String.valueOf(word.charAt(j)));
                    fourLetterLabels[row][j].setOpaque(true);
                    fourLetterLabels[row][j].setBackground(new Color(241, 196, 15));
                    animationManager.animateGridCell(fourLetterLabels[row][j]);
                }
            }
        } else {
            // 5-letter word
            for (int j = 0; j < 5; j++) {
                if (j < word.length()) {
                    fiveLetterLabels[j].setText(String.valueOf(word.charAt(j)));
                    fiveLetterLabels[j].setOpaque(true);
                    fiveLetterLabels[j].setBackground(new Color(241, 196, 15));
                    animationManager.animateGridCell(fiveLetterLabels[j]);
                }
            }
        }
    }

    public void autoRevealLetter(GameState gameState, JLabel[][] threeLetterLabels,
                                 JLabel[][] fourLetterLabels, JLabel[] fiveLetterLabels,
                                 WordScrambleGame game) {
        // Find words that haven't been found yet
        List<Integer> remainingWordIndices = new ArrayList<>();
        for (int i = 0; i < gameState.wordsFound.length; i++) {
            if (!gameState.wordsFound[i]) {
                remainingWordIndices.add(i);
            }
        }

        if (!remainingWordIndices.isEmpty()) {
            // Get a random word from the remaining words
            int randomIndex = remainingWordIndices.get((int)(Math.random() * remainingWordIndices.size()));
            String word = gameState.currentWordSet[randomIndex];

            // Reveal one letter in the appropriate grid
            revealLetterInGrid(word, randomIndex, threeLetterLabels, fourLetterLabels, fiveLetterLabels);

            // Show notification
            JOptionPane.showMessageDialog(game,
                    "Bonus! A letter has been revealed for finding 2 words!",
                    "Auto Reveal", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void revealLetterInGrid(String word, int wordIndex, JLabel[][] threeLetterLabels,
                                   JLabel[][] fourLetterLabels, JLabel[] fiveLetterLabels) {
        if (wordIndex < 3) {
            // 3-letter word
            int row = wordIndex;
            // Find all empty positions
            List<Integer> emptyPositions = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                if (threeLetterLabels[row][j].getText().isEmpty()) {
                    emptyPositions.add(j);
                }
            }
            // Reveal a random empty position
            if (!emptyPositions.isEmpty()) {
                int randomPos = emptyPositions.get((int)(Math.random() * emptyPositions.size()));
                threeLetterLabels[row][randomPos].setText(String.valueOf(word.charAt(randomPos)));
                threeLetterLabels[row][randomPos].setOpaque(true);
                threeLetterLabels[row][randomPos].setBackground(new Color(52, 152, 219)); // Blue for hint
                animationManager.animateGridCell(threeLetterLabels[row][randomPos]);
            }
        } else if (wordIndex < 5) {
            // 4-letter word
            int row = wordIndex - 3;
            // Find all empty positions
            List<Integer> emptyPositions = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                if (fourLetterLabels[row][j].getText().isEmpty()) {
                    emptyPositions.add(j);
                }
            }
            // Reveal a random empty position
            if (!emptyPositions.isEmpty()) {
                int randomPos = emptyPositions.get((int)(Math.random() * emptyPositions.size()));
                fourLetterLabels[row][randomPos].setText(String.valueOf(word.charAt(randomPos)));
                fourLetterLabels[row][randomPos].setOpaque(true);
                fourLetterLabels[row][randomPos].setBackground(new Color(52, 152, 219)); // Blue for hint
                animationManager.animateGridCell(fourLetterLabels[row][randomPos]);
            }
        } else {
            // 5-letter word
            // Find all empty positions
            List<Integer> emptyPositions = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                if (fiveLetterLabels[j].getText().isEmpty()) {
                    emptyPositions.add(j);
                }
            }
            // Reveal a random empty position
            if (!emptyPositions.isEmpty()) {
                int randomPos = emptyPositions.get((int)(Math.random() * emptyPositions.size()));
                fiveLetterLabels[randomPos].setText(String.valueOf(word.charAt(randomPos)));
                fiveLetterLabels[randomPos].setOpaque(true);
                fiveLetterLabels[randomPos].setBackground(new Color(52, 152, 219)); // Blue for hint
                animationManager.animateGridCell(fiveLetterLabels[randomPos]);
            }
        }
    }
}
