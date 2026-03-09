package comp4321.searchengine;

import comp4321.searchengine.Crawler;

import java.io.IOException;

public class Launcher {
    public static void main(String[] args) throws IOException {
        Crawler.parse(30);
    }
}
