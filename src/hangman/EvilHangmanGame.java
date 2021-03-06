package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame {
    private final HashSet<String> dictionary;
    private final SortedSet<Character> usedLetters;
    private Set<String> wordSet;
    private String word;

    public EvilHangmanGame() {
        dictionary = new HashSet<>();
        wordSet = dictionary;
        usedLetters = new TreeSet<>();
    }

    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        this.dictionary.clear();
        wordSet = this.dictionary;
        usedLetters.clear();
        Scanner scanner = new Scanner(dictionary);
        while (scanner.hasNext()) {
            String word = scanner.next();
            if (word.length() == wordLength) {
                this.dictionary.add(word.toLowerCase());
            }
        }
        word = "";
        for (int i = 0; i < wordLength; i++) {
            word = word.concat("-");
        }
        if (this.dictionary.size() == 0) {
            throw new EmptyDictionaryException(dictionary.getName(), wordLength);
        }
    }

    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        char guessLower = Character.toString(guess).toLowerCase().charAt(0);
        if (usedLetters.contains(guessLower)) {
            throw new GuessAlreadyMadeException();
        }
        usedLetters.add(guessLower);

        HashMap<String, Set<String>> map = getMap(guessLower);
        word = evilAlgorithm(map, guess);
        wordSet = map.get(word);
        int numCorrect = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == guessLower) {
                numCorrect++;
            }
        }
        if (numCorrect == 0) {
            System.out.println("Sorry, there are no " + guessLower + "'s");
        } else if (numCorrect == 1) {
            System.out.println("Yes, there is 1 " + guessLower);
        } else {
            System.out.println("Yes, there are " + numCorrect + " " + guessLower + "'s");
        }

        return wordSet;
    }

    private HashMap<String, Set<String>> getMap(char guess) {
        HashMap<String, Set<String>> map = new HashMap<>();
        for (String word : wordSet) {
            String pattern = "";
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == guess) {
                    pattern = pattern.concat(Character.toString(guess));
                } else {
                    pattern = pattern.concat(Character.toString(this.word.charAt(i)));
                }
            }
            if (map.containsKey(pattern)) {
                map.get(pattern).add(word);
            } else {
                Set<String> set = new HashSet<>();
                set.add(word);
                map.put(pattern, set);
            }
        }

        return map;
    }

    /**
     * >:)
     */
    private String evilAlgorithm(HashMap<String, Set<String>> map, char guess) {
        int biggestSetLength = 0;
        String biggestSetPattern = null;
        HashMap<String, Set<String>> biggestSets = new HashMap<>();
        for (String pattern : map.keySet()) {
            Set<String> set = map.get(pattern);
            if (set.size() > biggestSetLength) {
                biggestSets.clear();
                biggestSets.put(pattern, set);
                biggestSetLength = set.size();
                biggestSetPattern = pattern;
            } else if (set.size() == biggestSetLength) {
                biggestSets.put(pattern, set);
            }
        }

        if (biggestSets.size() == 1) {
            return biggestSetPattern;
        }
        // 1.
        for (String pattern: biggestSets.keySet()) {
            if (Objects.equals(pattern, word)) {
                return word;
            }
        }

        // 2.
        String sparsestPattern = null;
        int fewestLetters = word.length();
        HashMap<String, Set<String>> sparsestPatternSets = new HashMap<>();
        for (String pattern: biggestSets.keySet()) {
            int strippedWordLength = pattern.replace("-", "").length();
            if (strippedWordLength < fewestLetters) {
                sparsestPattern = pattern;
                sparsestPatternSets.clear();
                sparsestPatternSets.put(pattern, biggestSets.get(pattern));
                fewestLetters = strippedWordLength;
            } else if (strippedWordLength == fewestLetters) {
                sparsestPatternSets.put(pattern, biggestSets.get(pattern));
            }
        }
        if (sparsestPatternSets.size() == 1) {
            return sparsestPattern;
        }

        // 3.
        HashMap<String, Set<String>> rightmostSets = new HashMap<>(sparsestPatternSets);
        for (int i = 0; i < word.length(); i++) {
            HashMap<String, Set<String>> previousSet = new HashMap<>(sparsestPatternSets);
            for (String pattern: previousSet.keySet()) {
                if (pattern.charAt(i) == guess) {
                    rightmostSets.remove(pattern);
                }
            }
            if (rightmostSets.size() == 1) {
                for (String pattern : rightmostSets.keySet()) {
                    return pattern;
                }
            } else if (rightmostSets.size() == 0) {
                rightmostSets = previousSet;
            }
        }

        return biggestSetPattern;
    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return usedLetters;
    }

    public boolean getHasWonGame() {
        return (!word.contains("-"));
    }

    public int getNumGuessedCharacters() {
        return (word.replace("-", "").length());
    }

    public String getEndingWord() {
        if (getHasWonGame()) {
            return word;
        } else {
            for (String word : wordSet) {
                return word;
            }
        }

        return null;
    }

    public String getWord() {
        return word;
    }

    public Set<String> getWordSet() {
        return wordSet;
    }
}
