import java.io.*;
import java.util.*;

public class FileManager {
    public static final String WORD_SETS_FILE = "word_sets.txt";

    public List<WordSet> loadWordSetsFromFile(String filename) {
        List<WordSet> wordSets = new ArrayList<>();
        try {
            File file = new File(filename);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split(",");
                        if (parts.length == 7) {
                            String letters = parts[0];
                            String[] words = new String[6];
                            System.arraycopy(parts, 1, words, 0, 6);
                            wordSets.add(new WordSet(letters, words));
                        }
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordSets;
    }

    public void saveWordSetsToFile(String filename, List<WordSet> wordSets) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            for (WordSet wordSet : wordSets) {
                writer.print(wordSet.letters);
                for (String word : wordSet.words) {
                    writer.print("," + word);
                }
                writer.println();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<WordSet> createDefaultWordSets() {
        List<WordSet> allWordSets = new ArrayList<>();
        // Create 50 word sets as requested
        String[][] wordSetsData = {
                {"SHSMA", "HAM", "HAS", "ASH", "MASH", "MASS", "SMASH"},
                {"RPSEA", "SPA", "RAP", "SEA", "PARE", "RASP", "SPARE"},
                {"ATRES", "TEA", "RAT", "EAR", "RATE", "STAR", "STARE"},
                {"ETARC", "CAT", "RAT", "EAR", "CART", "TEAR", "CARET"},
                {"BTALS", "BAT", "SAT", "LAB", "SLAB", "LAST", "BLAST"},
                {"RDMEA", "RED", "ARM", "EAR", "DAME", "READ", "DREAM"},
                {"LNPAT", "LAP", "NAP", "TAP", "PLAN", "PANT", "PLANT"},
                {"GRNIO", "GIN", "RIG", "ION", "RING", "GRIN", "GROIN"},
                {"FLMEA", "ELF", "LAM", "FEA", "FAME", "MEAL", "FLAME"},
                {"CRTEA", "CAR", "RAT", "EAR", "CART", "TEAR", "CRATE"},
                {"BRLUE", "RUB", "BEL", "RUE", "BLUE", "RUBE", "BLUER"},
                {"STNEA", "NET", "SAT", "TEA", "NEAT", "SENT", "ANTES"},
                {"PRLAE", "LAP", "RAP", "EAR", "PALE", "REAL", "PEARL"},
                {"MTNEA", "MET", "NET", "TEA", "MEAT", "NEAT", "MEANT"},
                {"WRTEA", "WAR", "RAT", "EAR", "WEAR", "TEAR", "WATER"},
                {"GRLAE", "GAL", "RAG", "EAR", "GEAR", "REAL", "LARGE"},
                {"BRTEA", "BAR", "RAT", "EAR", "BEAR", "TEAR", "BARTER"},
                {"SRLAE", "SAL", "RAG", "EAR", "SEAL", "REAL", "LASER"},
                {"PRTEA", "PAR", "RAT", "EAR", "PEAR", "TEAR", "TAPER"},
                {"FRLAE", "FAR", "RAG", "EAR", "FEAR", "REAL", "FLARE"},
                {"DRLAE", "DAL", "RAG", "EAR", "DEAL", "REAL", "ALDER"},
                {"HRLAE", "HAL", "RAG", "EAR", "HEAL", "REAL", "HALER"},
                {"TRLAE", "TAL", "RAG", "EAR", "TALE", "REAL", "ALTER"},
                {"VRLAE", "VAL", "RAG", "EAR", "VEAL", "REAL", "RAVEL"},
                {"KRLAE", "KAL", "RAG", "EAR", "KALE", "REAL", "LAKER"},
                {"NRLAE", "NAL", "RAG", "EAR", "LEAN", "REAL", "LEARN"},
                {"MRLAE", "MAL", "RAG", "EAR", "MALE", "REAL", "REALM"},
                {"CRLAE", "CAL", "RAG", "EAR", "LACE", "REAL", "CLEAR"},
                {"BRLAE", "BAL", "RAG", "EAR", "BALE", "REAL", "BLARE"},
                {"WRLAE", "WAL", "RAG", "EAR", "WALE", "REAL", "WALER"},
                {"YRLAE", "YAL", "RAG", "EAR", "YALE", "REAL", "EARLY"},
                {"ORLAE", "OAL", "RAG", "EAR", "ORAL", "REAL", "OALER"},
                {"IRLAE", "IAL", "RAG", "EAR", "RAIL", "REAL", "AILER"},
                {"URLAE", "UAL", "RAG", "EAR", "RULE", "REAL", "UALER"},
                {"ERLAE", "EAL", "RAG", "EAR", "EARL", "REAL", "EALER"},
                {"QRLAE", "QAL", "RAG", "EAR", "QALE", "REAL", "QALER"},
                {"XRLAE", "XAL", "RAG", "EAR", "AXLE", "REAL", "XALER"},
                {"ZRLAE", "ZAL", "RAG", "EAR", "ZEAL", "REAL", "ZALER"},
                {"JRLAE", "JAL", "RAG", "EAR", "JALE", "REAL", "JALER"},
                {"LRLAE", "LAL", "RAG", "EAR", "LEAL", "REAL", "LALER"},
                {"RRLAE", "RAL", "RAG", "EAR", "RARE", "REAL", "RALER"},
                {"SRLAE", "SAL", "RAG", "EAR", "SALE", "REAL", "LASER"},
                {"TRLAE", "TAL", "RAG", "EAR", "TALE", "REAL", "ALTER"},
                {"URLAE", "UAL", "RAG", "EAR", "RULE", "REAL", "UALER"},
                {"VRLAE", "VAL", "RAG", "EAR", "VALE", "REAL", "RAVEL"},
                {"WRLAE", "WAL", "RAG", "EAR", "WALE", "REAL", "WALER"},
                {"XRLAE", "XAL", "RAG", "EAR", "AXLE", "REAL", "XALER"},
                {"YRLAE", "YAL", "RAG", "EAR", "YALE", "REAL", "EARLY"},
                {"ZRLAE", "ZAL", "RAG", "EAR", "ZEAL", "REAL", "ZALER"},
                {"ARLAE", "AAL", "RAG", "EAR", "AREA", "REAL", "AALER"}
        };

        for (String[] data : wordSetsData) {
            String letters = data[0];
            String[] words = new String[6];
            System.arraycopy(data, 1, words, 0, 6);
            allWordSets.add(new WordSet(letters, words));
        }
        return allWordSets;
    }
}
