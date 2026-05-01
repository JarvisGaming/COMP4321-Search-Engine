package hk.ust.cse.comp4321.project.nlp;

import opennlp.tools.stemmer.PorterStemmer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class StopwordStem {
    private final PorterStemmer  stemmer;
    private final Set<String> stopwords;

    public StopwordStem() {
        stemmer = new PorterStemmer();
        HashSet<String> temp;

        InputStream stream = StopwordStem.class.getResourceAsStream("/stopwords.txt");
        assert stream != null;
        InputStreamReader isReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader bReader = new BufferedReader(isReader);
        temp = bReader.lines().collect(Collectors.toCollection(HashSet::new));

        stopwords = temp;
    }

    public boolean isStopword(String word) {
        return stopwords.contains(word);
    }

    public String stem(String word) {
        return stemmer.stem(word);
    }
}
