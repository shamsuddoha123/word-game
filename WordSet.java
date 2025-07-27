public class WordSet {
    public String letters;
    public String[] words = new String[6]; // 3 three-letter, 2 four-letter, 1 five-letter

    public WordSet(String letters, String[] words) {
        this.letters = letters;
        this.words = words;
    }
}
