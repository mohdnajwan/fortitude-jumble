package asia.fourtitude.interviewq.jumble.core;

import java.io.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.thymeleaf.util.ListUtils;

public class JumbleEngine {

    /**
     * From the input `word`, produces/generates a copy which has the same
     * letters, but in different ordering.
     *
     * Example: from "elephant" to "aeehlnpt".
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#scramble()
     * b) scrambled letters/output must not be the same as input
     *
     * @param word  The input word to scramble the letters.
     * @return  The scrambled output/letters.
     */
    public String scramble(String word) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */

        if(null == word || word.isEmpty()){
            throw new UnsupportedOperationException("Scramble word must not null");
        }

        boolean sameWord = true;
        boolean finish = false;
        List<Integer> sameIndex = new ArrayList<Integer>();
        StringBuilder scrambled = new StringBuilder();
        Random random = new Random();

        while(sameWord){
            while(!finish){
                int index = random.nextInt(word.length());
                if(!sameIndex.contains(index)){
                    scrambled.append(word.charAt(index));
                    sameIndex.add(index);
                }
                if(sameIndex.size() == word.length()) finish = true;
            }

            if(!word.equals(scrambled.toString())){
                sameWord = false;
            }else{
                sameIndex = new ArrayList<>();
                scrambled = new StringBuilder();
            }
        }

        return scrambled.toString();
    }

    /**
     * Retrieves the palindrome words from the internal
     * word list/dictionary ("src/main/resources/words.txt").
     *
     * Word of single letter is not considered as valid palindrome word.
     *
     * Examples: "eye", "deed", "level".
     *
     * Evaluation/Grading:
     * a) able to access/use resource from classpath
     * b) using inbuilt Collections
     * c) using "try-with-resources" functionality/statement
     * d) pass unit test: JumbleEngineTest#palindrome()
     *
     * @return  The list of palindrome words found in system/engine.
     * @see https://www.google.com/search?q=palindrome+meaning
     */
    public Collection<String> retrievePalindromeWords() {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */
        Collection<String> wordList = new ArrayList<>();
        Collection<String> palindromes = new ArrayList<>();

        try {
            wordList = readWordFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Failed to read the file.");
        }

        for (String word : wordList) {
            if(word.length() == 1) continue;
            String reversed = new StringBuilder(word).reverse().toString();
            if (reversed.equalsIgnoreCase(word)) {
                palindromes.add(word);
            }
        }

        return palindromes;
    }

    /**
     * Picks one word randomly from internal word list.
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#randomWord()
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param length  The word picked, must of length.
     *                When length is null, then return random word of any length.
     * @return  One of the word (randomly) from word list.
     *          Or null if none matching.
     */
    public String pickOneRandomWord(Integer length) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */
        Collection<String> wordList = new ArrayList<>();
        Random random = new Random();

        try {
            wordList = readWordFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Failed to read the file.");
        }

        List<String> randomWordList = new ArrayList<>();
        if (length == null) {
            for (int i = 0; i < wordList.size(); i++) {
                int randomLength = random.nextInt(10) + 1;
                randomWordList = wordList.stream()
                        .filter(e -> e.length() == randomLength)
                        .collect(Collectors.toList());
                if (!randomWordList.isEmpty()) {
                    return randomWordList.get(random.nextInt(randomWordList.size()));
                }
            }
            return null;
        } else {
            randomWordList = wordList.stream()
                    .filter(e -> e.length() == length)
                    .collect(Collectors.toList());
            if (randomWordList.isEmpty()) {
                return null;
            }
            return randomWordList.get(random.nextInt(randomWordList.size()));
        }
    }

    /**
     * Checks if the `word` exists in internal word list.
     * Matching is case insensitive.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word  The input word to check.
     * @return  true if `word` exists in internal word list.
     */
    public boolean exists(String word) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */
        Collection<String> wordList = new ArrayList<>();

        try {
            wordList = readWordFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Failed to read the file.");
        }

        return wordList.stream().anyMatch(e -> e.equalsIgnoreCase(word));
    }

    /**
     * Finds all the words from internal word list which begins with the
     * input `prefix`.
     * Matching is case insensitive.
     *
     * Invalid `prefix` (null, empty string, blank string, non letter) will
     * return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param prefix  The prefix to match.
     * @return  The list of words matching the prefix.
     */
    public Collection<String> wordsMatchingPrefix(String prefix) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */

        Collection<String> wordList = new ArrayList<>();

        if(null == prefix || prefix.isEmpty() || !prefix.matches("^[A-Za-z]+$")){
            return wordList;
        }

        try {
            wordList = readWordFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Failed to read the file.");
        }

        return wordList.stream().filter(e -> e.startsWith(prefix.toLowerCase())).collect(Collectors.toList());
    }

    /**
     * Finds all the words from internal word list that is matching
     * the searching criteria.
     *
     * `startChar` and `endChar` must be 'a' to 'z' only. And case insensitive.
     * `length`, if have value, must be positive integer (>= 1).
     *
     * Words are filtered using `startChar` and `endChar` first.
     * Then apply `length` on the result, to produce the final output.
     *
     * Must have at least one valid value out of 3 inputs
     * (`startChar`, `endChar`, `length`) to proceed with searching.
     * Otherwise, return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param startChar  The first character of the word to search for.
     * @param endChar    The last character of the word to match with.
     * @param length     The length of the word to match.
     * @return  The list of words matching the searching criteria.
     */
    public Collection<String> searchWords(Character startChar, Character endChar, Integer length) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */
        Collection<String> wordList = new ArrayList<>();

        if(null == startChar && null == endChar && null == length){
            return wordList;
        }
        if(null != startChar && Character.isDigit(startChar)){
            return wordList;
        } 
        if(null != endChar && Character.isDigit(endChar)){
            return wordList;
        }
        if(null != length && length < 0){
            return wordList;
        }

        try {
            wordList = readWordFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Failed to read the file.");
        }

        return wordList.stream()
        .map(String::toLowerCase)
        .filter(word -> {
                boolean matches = true;

                if (null != startChar) {
                    matches &= word.startsWith(String.valueOf(Character.toLowerCase(startChar)));
                }

                if (null != endChar) {
                    matches &= word.endsWith(String.valueOf(Character.toLowerCase(endChar)));
                }

                if (null != length) {
                    matches &= word.length() == length;
                }

                return matches;
            }
        )
        .collect(Collectors.toList());
    }

    /**
     * Generates all possible combinations of smaller/sub words using the
     * letters from input word.
     *
     * The `minLength` set the minimum length of sub word that is considered
     * as acceptable word.
     *
     * If length of input `word` is less than `minLength`, then return empty list.
     *
     * The sub words must exist in internal word list.
     *
     * Example: From "yellow" and `minLength` = 3, the output sub words:
     *     low, lowly, lye, ole, owe, owl, well, welly, woe, yell, yeow, yew, yowl
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word       The input word to use as base/seed.
     * @param minLength  The minimum length (inclusive) of sub words.
     *                   Expects positive integer.
     *                   Default is 3.
     * @return  The list of sub words constructed from input `word`.
     */
    public Collection<String> generateSubWords(String word, Integer minLength) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */

        Collection<String> generatedSubWords = new ArrayList<>();

        // Validate input
        if (word == null || word.trim().isEmpty() || !word.matches("^[A-Za-z]+$")) {
            return generatedSubWords;
        }

        if (minLength == null) {
            minLength = 3;
        }

        if (word.length() < minLength || minLength < 1) {
            return generatedSubWords;
        }

        Collection<String> words;
        try {
            words = readWordFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Failed to read the file.");
        }

        Map<Character, Long> wordFreq = word.toLowerCase()
                .chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        for (String dictWord : words) {
            if (dictWord.length() < minLength) continue;
            if (dictWord.equalsIgnoreCase(word)) continue; // Don't include original word
            if (canFormFromWord(dictWord.toLowerCase(), wordFreq)) {
                generatedSubWords.add(dictWord);
            }
        }

        return generatedSubWords;
    }

    // Help from AI
    private boolean canFormFromWord(String candidate, Map<Character, Long> sourceFreq) {
        Map<Character, Long> candidateFreq = candidate.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        for (Map.Entry<Character, Long> entry : candidateFreq.entrySet()) {
            if (sourceFreq.getOrDefault(entry.getKey(), 0L) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a game state with word to guess, scrambled letters, and
     * possible combinations of words.
     *
     * Word is of length 6 characters.
     * The minimum length of sub words is of length 3 characters.
     *
     * @param length     The length of selected word.
     *                   Expects >= 3.
     * @param minLength  The minimum length (inclusive) of sub words.
     *                   Expects positive integer.
     *                   Default is 3.
     * @return  The game state.
     */
    public GameState createGameState(Integer length, Integer minLength) {
        Objects.requireNonNull(length, "length must not be null");
        if (minLength == null) {
            minLength = 3;
        } else if (minLength <= 0) {
            throw new IllegalArgumentException("Invalid minLength=[" + minLength + "], expect positive integer");
        }
        if (length < 3) {
            throw new IllegalArgumentException("Invalid length=[" + length + "], expect greater than or equals 3");
        }
        if (minLength > length) {
            throw new IllegalArgumentException("Expect minLength=[" + minLength + "] greater than length=[" + length + "]");
        }
        String original = this.pickOneRandomWord(length);
        if (original == null) {
            throw new IllegalArgumentException("Cannot find valid word to create game state");
        }
        String scramble = this.scramble(original);
        Map<String, Boolean> subWords = new TreeMap<>();
        for (String subWord : this.generateSubWords(original, minLength)) {
            subWords.put(subWord, Boolean.FALSE);
        }
        return new GameState(original, scramble, subWords);
    }

    private Collection<String> readWordFile() throws IOException{

        Collection<String> wordList = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/words.txt"));
            try {
                String line = br.readLine();

                while (line != null) {
                    wordList.add(line);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }

        return wordList;
    }

}
