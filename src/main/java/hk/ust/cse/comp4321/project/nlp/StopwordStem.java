package hk.ust.cse.comp4321.project.nlp;

import edu.stanford.nlp.process.Morphology;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class StopwordStem {
    private final Morphology morphology;
    private final Set<String> stopwords;

    public StopwordStem() {
        morphology = new Morphology();
        HashSet<String> temp;

        InputStream stream = StopwordStem.class.getResourceAsStream("/stopwords.txt");
        InputStreamReader isReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader bReader = new BufferedReader(isReader);
        temp = bReader.lines().collect(Collectors.toCollection(HashSet::new));

        stopwords = temp;
    }

    public boolean isStopword(String word) {
        return stopwords.contains(word);
    }

    public String stem(String word) {
        return morphology.stem(word);
    }
}
