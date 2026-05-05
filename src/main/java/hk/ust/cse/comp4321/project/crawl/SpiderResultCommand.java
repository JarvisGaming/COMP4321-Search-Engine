package hk.ust.cse.comp4321.project.crawl;

import hk.ust.cse.comp4321.project.index.RecordIndex;
import picocli.CommandLine.Command;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;


@Command(name = "spider_result", description = "Create spider_result.txt using RecordIndex")
public class SpiderResultCommand implements Runnable {
    @Override
    public void run() {
        try {
            RecordIndex recordIndex = RecordIndex.getInstance();
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("spider_result.txt"), StandardCharsets.UTF_8));

            recordIndex.stream()
                .map(Map.Entry::getValue)
                .forEach(record -> {
                    try {
                        writer.write(record.title() + '\n');
                        writer.write(record.url().toString() + '\n');
                        writer.write("Last Modified: " + record.lastModificationTimestamp().toString() + " , Size: " + record.pageSize() + '\n');

                        String keywordFreqPairs = record.bodyTermFrequencies().entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .limit(10)
                            .map(e -> e.getKey() + " " + e.getValue())
                            .collect(Collectors.joining("; ")) + ";";
                        writer.write(keywordFreqPairs + '\n');

                        String urls = record.childURLs().stream()
                            .limit(10)
                            .map(URL::toString)
                            .collect(Collectors.joining("\n"));
                        writer.write(urls + '\n');

                        writer.write("\n===\n\n");

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
