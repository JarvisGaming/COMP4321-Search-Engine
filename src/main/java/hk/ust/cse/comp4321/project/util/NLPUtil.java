package hk.ust.cse.comp4321.project.util;

import hk.ust.cse.comp4321.project.nlp.StopwordStem;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class NLPUtil {
    private static final StopwordStem stopwordStem = new StopwordStem();
    private static final String TOKENIZER_DELIMITERS = " \t\n\r\f\"'\\()[]<>:,.?!@#$%^&*-_=+|/";

    // Prepare document body/title or query for downstream tasks
    public static List<String> standardizeWords(List<String> words) {
        return words.stream()
                .map(String::toLowerCase)
                .filter(NLPUtil::isAlphaNumeric)
                .toList();
    }

    public static List<String> removeStopwordsAndStem(List<String> words) {
        return words.stream()
                .filter(NLPUtil::isNotStopword)
                .map(NLPUtil::stem)
                .toList();
    }

    public static List<String> extractWords(@NotNull Document document) {
        List<String> wordList = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(document.body().text(), TOKENIZER_DELIMITERS);

        while (tokenizer.hasMoreTokens())
            wordList.add(tokenizer.nextToken());

        return wordList;
    }

    public static List<String> extractWords(String words) {
        List<String> wordList = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(words, TOKENIZER_DELIMITERS);

        while (tok.hasMoreTokens())
            wordList.add(tok.nextToken());

        return wordList;
    }

    public static boolean isAlphaNumeric(String word) {
        return word != null && word.matches("^[a-zA-Z0-9]+$");
    }

    public static boolean isNotStopword(@NotNull String word) {
        return !stopwordStem.isStopword(word);
    }

    public static String stem(@NotNull String word) {
        return stopwordStem.stem(word);
    }
}
